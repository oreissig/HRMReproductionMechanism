package com.github.oreissig.hrm.backend.interpreter

import spock.lang.Ignore
import spock.lang.Stepwise
import spock.lang.Unroll

import com.github.oreissig.hrm.AbstractHRMSpec

@Stepwise
@Unroll
class InterpreterSpec extends AbstractHRMSpec
{
    def walker = new InterpreterWalker()
    def i = Mock(InputStream)
    def o = Mock(PrintStream)
    
    def setup() {
        InterpreterListener.input = i
        InterpreterListener.output = o
    }
    
    def 'empty program interprets successfully'() {
        given:
        input = ''
        
        when:
        walker.interpret(parse())
        
        then:
        0 * _
    }
    
    def 'I/O works (#value)'(value) {
        given:
        input = '''\
                inbox
                outbox
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.read() >> value
        1 * o.print(value)
        0 * _
        
        where:
        value << [23, -42, 0, Integer.MAX_VALUE, Integer.MIN_VALUE]
    }
    
    def 'copy from/to work (#value)'(value) {
        given:
        // swap two inputs
        input = """\
                inbox
                copyto $value
                inbox
                outbox
                copyfrom $value
                outbox
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        with(i) {
            1 * read() >> 123
            1 * read() >> 456
        }
        then:
        1 * o.print(456)
        then:
        1 * o.print(123)
        0 * _
        
        where:
        value << [10, InterpreterListener.MAX_MEM-1, 0]
    }
    
    def 'add works (#a+#b=#sum)'(a, b, sum) {
        given:
        input = '''\
                inbox
                copyto 1
                inbox
                add 1
                outbox
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        with(i) {
            1 * read() >> b
            1 * read() >> a
        }
        1 * o.print(sum)
        0 * _
        
        where:
        a   | b   | sum
        123 | 543 | 666
        42  | -23 | 19
        -23 | -42 | -65
        23  | 0   | 23
        0   | 42  | 42
        0   | 0   | 0
    }
    
    def 'sub works (#a-#b=#diff)'(a, b, diff) {
        given:
        input = '''\
                inbox
                copyto 1
                inbox
                sub 1
                outbox
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        with(i) {
            1 * read() >> b
            1 * read() >> a
        }
        1 * o.print(diff)
        0 * _
        
        where:
        a   | b   | diff
        666 | 543 | 123
        -42 | 23  | -65
        -23 | -42 | 19
        23  | 0   | 23
        0   | 42  | -42
        0   | 0   | 0
    }
    
    def '#bump works (#bump(#value)=#result)'(bump, value, result) {
        given:
        input = """\
                inbox
                copyto 1
                $bump 1
                copyfrom 1
                outbox
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.read() >> value
        1 * o.print(result)
        0 * _
        
        where:
        bump    | value | result
        'bump+' | 10    | 11
        'bump-' | 10    | 9
    }
    
    def 'jump works'() {
        given:
        input = """\
                inbox
                jump foo
                inbox
                :foo
                outbox
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.read() >> 23
        1 * o.print(23)
        0 * _
    }
    
    @Ignore('TODO')
    def 'jump if zero works'() {
        given:
        input = """\
                inbox
                jump if zero foo
                inbox
                :foo
                inbox
                outbox
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.read() >>> inbox
        1 * o.print(result)
        0 * _
        
        where:
        inbox      | result
        [-3, 1, 2] | 2
        [ 3, 1, 2] | 2
        [ 0, 1, 2] | 1
    }
    
    @Ignore('TODO')
    def 'jump if negative works'(inbox, result) {
        given:
        input = """\
                inbox
                jump if negative foo
                inbox
                :foo
                inbox
                outbox
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.read() >>> inbox
        1 * o.print(result)
        0 * _
        
        where:
        inbox      | result
        [-3, 1, 2] | 1
        [ 3, 1, 2] | 2
        [ 0, 1, 2] | 2
    }
}
