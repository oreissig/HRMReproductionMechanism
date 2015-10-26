package com.github.oreissig.hrm.backend.interpreter

import spock.lang.Stepwise
import spock.lang.Unroll

import com.github.oreissig.hrm.AbstractHRMSpec

@Stepwise
@Unroll
class InterpreterSpec extends AbstractHRMSpec
{
    static final didJump = 1
    static final didNotJump = 2
    
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
    
    def 'outbox clears the hand value'() {
        given:
        input = '''\
                inbox
                outbox
                outbox
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        thrown(EmptyHandsException)
        1 * i.read() >> 23
        1 * o.print(23)
        0 * _
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
    
    def 'add works (#a + #b = #sum)'(a, b, sum) {
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
    
    def 'sub works (#a - #b = #diff)'(a, b, diff) {
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
    
    def '#bump works (#bump(#value) = #result)'(bump, value, result) {
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
    
    def 'jump throws exception for unknown label'() {
        given:
        input = 'jump foo'
        
        when:
        walker.interpret(parse())
        
        then:
        thrown(UnknownJumpLabelException)
    }
    
    def 'jump if zero works (#value)'(value, result) {
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
        i.read() >>> [value, didJump, didNotJump]
        1 * o.print(result)
        0 * _
        
        where:
        value | result
        -3    | 2
         3    | 2
         0    | 1
    }
    
    def 'jump if negative works (#value)'(value, result) {
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
        i.read() >>> [value, didJump, didNotJump]
        1 * o.print(result)
        0 * _
        
        where:
        value | result
        -3    | didJump
         3    | didNotJump
         0    | didNotJump
    }
    
    def 'empty hands throw exception for "#name"'(name, src) {
        given:
        input = src
        
        when:
        walker.interpret(parse())
        
        then:
        EmptyHandsException ex = thrown()
        ex.message.contains name
        name.startsWith ex.location.start.text
        ex.stackTrace.size() == 1
        ex.stackTrace[0].lineNumber == 1
        
        where:
        name               | src
        'outbox'           | 'outbox'
        'copyto'           | 'copyto 1'
        'add'              | 'add 1'
        'sub'              | 'sub 1'
        'jump if zero'     | 'jump if zero foo'
        'jump if negative' | 'jump if negative foo'
    }
    
    def 'empty tile throws exception for "#name"'(name, src) {
        given:
        i.read() >> 0
        input = """\
                inbox
                $src
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        EmptyTileException ex = thrown()
        ex.message.contains name
        name.startsWith ex.location.start.text
        ex.stackTrace.size() == 1
        ex.stackTrace[0].lineNumber == 2
        
        where:
        name               | src
        'copyfrom'         | 'copyfrom 1'
        'add'              | 'add 1'
        'sub'              | 'sub 1'
        'bump+'            | 'bump+ 1'
        'bump-'            | 'bump- 1'
    }
}
