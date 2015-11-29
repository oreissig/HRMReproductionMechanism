package com.github.oreissig.hrm.backend.interpreter

import spock.lang.Stepwise
import spock.lang.Unroll

import com.github.oreissig.hrm.AbstractHRMSpec

@Stepwise
@Unroll
class InterpreterSpec extends AbstractHRMSpec
{
    static final didJump = '1'
    static final didNotJump = '2'
    
    def walker = new InterpreterWalker()
    BufferedReader i = Mock()
    PrintWriter o = Mock()
    
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
        // no invocations at all
        0 * _
    }
    
    def 'comments are ignored'() {
        given:
        input = 'COMMENT 1'
        
        when:
        walker.interpret(parse())
        
        then:
        // no invocations at all
        0 * _
    }
    
    def 'I/O works (#value)'(value) {
        given:
        input = '''\
                INBOX
                OUTBOX
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * o.print('> ')
        1 * i.readLine() >> value
        1 * o.println(value)
        
        where:
        value << [23, -42, 0, Integer.MAX_VALUE, Integer.MIN_VALUE]*.toString()
    }
    
    def 'inbox end is signaled'(value) {
        given:
        input = 'INBOX'
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.readLine() >> value
        EmptyInboxException ex = thrown()
        ex.message == 'The end of the inbox has been reached!'
        
        where:
        value << [null, '']
    }
    
    def 'outbox clears the hand value'() {
        given:
        input = '''\
                INBOX
                OUTBOX
                OUTBOX
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        thrown(EmptyHandsException)
        1 * i.readLine() >> '23'
        1 * o.println('23')
    }
    
    def 'copy from/to work (#value)'(value) {
        given:
        // swap two inputs
        input = """\
                INBOX
                COPYTO $value
                INBOX
                OUTBOX
                COPYFROM $value
                OUTBOX
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        with(i) {
            1 * readLine() >> '123'
            1 * readLine() >> '456'
        }
        then:
        1 * o.println('456')
        then:
        1 * o.println('123')
        
        where:
        value << [10, InterpreterListener.MAX_TILE-1, 0]
    }
    
    def 'copy from/to support indirect addressing (#addr)'(addr,value) {
        given:
        // select 1 or 2 based on 3
        input = '''\
                INBOX
                COPYTO 1
                INBOX
                COPYTO [1]
                INBOX
                COPYTO 3
                COPYFROM [3]
                OUTBOX
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        with(i) {
            1 * readLine() >> '2'
            1 * readLine() >> '1'
            1 * readLine() >> addr
        }
        then:
        1 * o.println(value)
        
        where:
        addr | value
        1    | '2'
        2    | '1'
    }
    
    def 'add works (#a + #b = #sum)'(a, b, sum) {
        given:
        input = '''\
                INBOX
                COPYTO 1
                INBOX
                ADD 1
                OUTBOX
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        with(i) {
            1 * readLine() >> b
            1 * readLine() >> a
        }
        1 * o.println(sum.toString())
        
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
                INBOX
                COPYTO 1
                INBOX
                SUB 1
                OUTBOX
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        with(i) {
            1 * readLine() >> b
            1 * readLine() >> a
        }
        1 * o.println(diff.toString())
        
        where:
        a   | b   | diff
        666 | 543 | 123
        -42 | 23  | -65
        -23 | -42 | 19
        23  | 0   | 23
        0   | 42  | -42
        0   | 0   | 0
    }
    
    def '#addOp supports indirect addressing'(addOp,sum) {
        given:
        input = """\
                INBOX
                COPYTO 1
                INBOX
                COPYTO 2
                ${addOp.toUpperCase()} [1]
                OUTBOX
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        with(i) {
            1 * readLine() >> 2
            1 * readLine() >> 1
        }
        1 * o.println(sum)
        
        where:
        addOp | sum
        'add' | '2'
        'sub' | '0'
    }
    
    def '#bump works (#bump(#value) = #result)'(bump, value, result) {
        given:
        input = """\
                INBOX
                COPYTO 1
                $bump 1
                OUTBOX
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.readLine() >> value
        1 * o.println(result.toString())
        
        where:
        bump     | value | result
        'BUMPUP' | 10    | 11
        'BUMPDN' | 10    | 9
    }
    
    def '#bump supports indirect addressing'(bump,result) {
        given:
        // bump 1 or 2 based on 3
        input = """\
                INBOX
                COPYTO 1
                INBOX
                COPYTO 2
                INBOX
                COPYTO 3
                $bump [3]
                OUTBOX
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        with(i) {
            1 * readLine() >> 1
            1 * readLine() >> 2
            1 * readLine() >> 2
        }
        then:
        1 * o.println(result)
        
        where:
        bump     | result
        'BUMPUP' | '3'
        'BUMPDN' | '1'
    }
    
    def 'jump works'() {
        given:
        input = """\
                INBOX
                JUMP foo
                INBOX
                foo:
                OUTBOX
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.readLine() >> '23'
        1 * o.println('23')
    }
    
    def 'jump throws exception for unknown label'() {
        given:
        input = 'JUMP foo'
        
        when:
        walker.interpret(parse())
        
        then:
        thrown(UnknownJumpLabelException)
    }
    
    def 'jump if zero works (#value)'(value, result) {
        given:
        input = """\
                INBOX
                JUMPZ foo
                INBOX
                foo:
                INBOX
                OUTBOX
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        i.readLine() >>> [value, didJump, didNotJump]
        1 * o.println(result)
        
        where:
        value | result
        -3    | didNotJump
         3    | didNotJump
         0    | didJump
    }
    
    def 'jump if negative works (#value)'(value, result) {
        given:
        input = """\
                INBOX
                JUMPN foo
                INBOX
                foo:
                INBOX
                OUTBOX
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        i.readLine() >>> [value, didJump, didNotJump]
        1 * o.println(result)
        
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
        name     | src
        'OUTBOX' | 'OUTBOX'
        'COPYTO' | 'COPYTO 1'
        'ADD'    | 'ADD 1'
        'SUB'    | 'SUB 1'
        'JUMPN'  | 'JUMPN foo'
        'JUMPN'  | 'JUMPN foo'
    }
    
    def 'empty tile throws exception for "#name"'(name, src) {
        given:
        i.readLine() >> '0'
        input = """\
                INBOX
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
        name       | src
        'COPYFROM' | 'COPYFROM 1'
        'ADD'      | 'ADD 1'
        'SUB'      | 'SUB 1'
        'BUMPUP'   | 'BUMPUP 1'
        'BUMPDN'   | 'BUMPDN 1'
    }
    
    def 'literal mode initializes tiles with their index'() {
        given:
        InterpreterListener.LITERAL_MODE = true
        input = '''\
                COPYFROM 42
                SUB 23
                OUTBOX
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * o.println('19')
        
        cleanup:
        InterpreterListener.LITERAL_MODE = false
    }
    
    def 'debug info can be printed with DUMP'() {
        given:
        input = """\
                INBOX
                COPYTO 1
                COPYTO 5
                BUMPUP 5
                DUMP
                """.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.readLine() >> 23
        1 * o.println('HANDS: 24')
        1 * o.println('FLOOR TILE 1: 23')
        1 * o.println('FLOOR TILE 5: 24')
        0 * o.println(_) // no output for empty tiles
    }
}
