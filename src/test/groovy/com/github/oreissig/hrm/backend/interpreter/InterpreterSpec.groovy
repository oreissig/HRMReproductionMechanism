package com.github.oreissig.hrm.backend.interpreter

import com.github.oreissig.hrm.AbstractHRMSpec
import spock.lang.Unroll

import static com.github.oreissig.hrm.backend.interpreter.InterpreterListener.MAX_TILE
import static com.github.oreissig.hrm.backend.interpreter.InterpreterListener.MAX_VALUE
import static com.github.oreissig.hrm.backend.interpreter.InterpreterListener.MIN_VALUE

@Unroll
class InterpreterSpec extends AbstractHRMSpec {
    def walker = new InterpreterWalker()
    InterpreterContext context = Mock()

    def 'empty program interprets successfully'() {
        given:
        input = ''

        when:
        walker.interpret(parse(), context)

        then:
        // no invocations at all
        0 * _
    }

    def 'comments are ignored'() {
        given:
        input = 'COMMENT 1'

        when:
        walker.interpret(parse(), context)

        then:
        // no invocations at all
        0 * _
    }

    def 'input works (#value)'(value) {
        given:
        input = '''\
                INBOX
                '''.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.read() >> value

        where:
        value << [23, -42, 0, MAX_VALUE, MIN_VALUE]
    }

    def 'output works (#value)'(value) {
        given:
        input = '''\
                INBOX
                OUTBOX
                '''.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.read() >> value
        1 * context.print(value)

        where:
        value << [23, -42, 0, MAX_VALUE, MIN_VALUE]
    }

    def 'inbox end is signaled'() {
        given:
        input = 'INBOX'

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.read() >> null
        EmptyInboxException ex = thrown()
        ex.message == 'The end of the inbox has been reached!'
    }

    def 'outbox clears the hand value'() {
        given:
        input = '''\
                INBOX
                OUTBOX
                OUTBOX
                '''.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        thrown(EmptyHandsException)
        1 * context.read() >> 23
        1 * context.print(23)
    }

    def 'copyfrom works (#value)'(address) {
        given:
        def value = 123
        input = """\
                COPYFROM $address
                OUTBOX
                """.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.getAt(address) >> value
        1 * context.print(value)

        where:
        address << [10, MAX_TILE - 1, 0]
    }

    def 'copyto works (#value)'(address) {
        given:
        def value = 123
        input = """\
                INBOX
                COPYTO $address
                """.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.read() >> value
        1 * context.putAt(address, value)

        where:
        address << [10, MAX_TILE - 1, 0]
    }

    def 'copyfrom supports indirect addressing'() {
        given:
        def value = 123
        def pointer = 23
        def target = 42
        input = """\
                COPYFROM [$pointer]
                OUTBOX
                """.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.getAt(pointer) >> target
        1 * context.getAt(target) >> value
        1 * context.print(value)
    }

    def 'copyto supports indirect addressing'() {
        given:
        def value = 123
        def pointer = 23
        def target = 42
        input = """\
                INBOX
                COPYTO [$pointer]
                """.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.read() >> value
        1 * context.getAt(pointer) >> target
        1 * context.putAt(target, value)
    }

    def 'address overflow throws an exception'() {
        given:
        def addr = MAX_TILE
        input = "COPYFROM $addr"

        when:
        walker.interpret(parse(), context)

        then:
        BadTileAddressException ex = thrown()
        ex.badAddress == addr
    }

    def 'address underflow throws an exception'() {
        given:
        def addr = -1
        // workaround because direct addressing doesn't parse negative numbers
        input = '''\
                COPYFROM [1]
                '''.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.getAt(1) >> addr
        BadTileAddressException ex = thrown()
        ex.badAddress == addr
    }

    def 'add works (#a + #b = #sum)'(a, b, sum) {
        given:
        input = '''\
                INBOX
                ADD 1
                OUTBOX
                '''.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        with(context) {
            1 * read() >> a
            1 * getAt(1) >> b
            1 * print(sum)
        }

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
                SUB 1
                OUTBOX
                '''.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        with(context) {
            1 * read() >> a
            1 * getAt(1) >> b
            1 * print(diff)
        }

        where:
        a   | b   | diff
        666 | 543 | 123
        -42 | 23  | -65
        -23 | -42 | 19
        23  | 0   | 23
        0   | 42  | -42
        0   | 0   | 0
    }

    def '#addOp supports indirect addressing'(addOp, sum) {
        given:
        input = """\
                INBOX
                ${addOp.toUpperCase()} [1]
                OUTBOX
                """.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        with(context) {
            1 * read() >> 1
            1 * getAt(1) >> 2
            1 * getAt(2) >> 1
            1 * print(sum)
        }

        where:
        addOp | sum
        'add' | 2
        'sub' | 0
    }

    def 'arithmetic #whatflow throws an exception'(whatflow, initial, operation) {
        given:
        input = """\
                INBOX
                $operation 1
                """.stripIndent()
        1 * context.read() >> initial
        1 * context.getAt(1) >> 1

        when:
        walker.interpret(parse(), context)

        then:
        OverflowException ex = thrown()
        ex.message.endsWith 'That should be enough for anybody.'
        ex.illegalValue == initial + Integer.signum(initial) // add one to magnitude

        where:
        whatflow    | initial   | operation
        'overflow'  | MAX_VALUE | 'ADD'
        'underflow' | MIN_VALUE | 'SUB'
    }

    def '#bump works (#bump(#value) = #result)'(bump, value, result) {
        given:
        input = "$bump 1"

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.getAt(1) >> value
        1 * context.putAt(1, result)

        where:
        bump     | value | result
        'BUMPUP' | 10    | 11
        'BUMPDN' | 10    | 9
    }

    def '#bump supports indirect addressing'(bump, value, result) {
        given:
        input = "$bump [1]"

        when:
        walker.interpret(parse(), context)

        then:
        with(context) {
            1 * getAt(1) >> 42
            1 * getAt(42) >> value
            1 * putAt(42, result)
        }

        where:
        bump     | value | result
        'BUMPUP' | 10    | 11
        'BUMPDN' | 10    | 9
    }

    def 'bump #whatflow throws an exception'(whatflow, initial, operation) {
        given:
        input = "$operation 1"
        1 * context.getAt(1) >> initial

        when:
        walker.interpret(parse(), context)

        then:
        OverflowException ex = thrown()
        ex.message.endsWith 'That should be enough for anybody.'
        ex.illegalValue == initial + Integer.signum(initial) // add one to magnitude

        where:
        whatflow    | initial   | operation
        'overflow'  | MAX_VALUE | 'BUMPUP'
        'underflow' | MIN_VALUE | 'BUMPDN'
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
        walker.interpret(parse(), context)

        then:
        1 * context.read() >> 23
        1 * context.print(23)
    }

    def 'jump throws exception for unknown label'() {
        given:
        input = 'JUMP foo'

        when:
        walker.interpret(parse(), context)

        then:
        thrown(UnknownJumpLabelException)
    }

    def 'jump if zero works (#value)'(value, jumped) {
        given:
        input = """\
                INBOX
                JUMPZ foo
                COPYTO 1
                foo:
                """.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.read() >> value
        (jumped ? 0 : 1) * context.putAt(1, value)

        where:
        value | jumped
        -3    | false
         3    | false
         0    | true
    }

    def 'jump if negative works (#value)'(value, jumped) {
        given:
        input = """\
                INBOX
                JUMPN foo
                COPYTO 1
                foo:
                """.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.read() >> value
        (jumped ? 0 : 1) * context.putAt(1, value)

        where:
        value | jumped
        -3    | true
         3    | false
         0    | false
    }

    def 'empty hands throw exception for "#name"'(name, src) {
        given:
        input = src

        when:
        walker.interpret(parse(), context)

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
        context.read() >> '0'
        input = """\
                INBOX
                $src
                """.stripIndent()

        when:
        walker.interpret(parse(), context)

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

    def 'indirect addressing throws exception for empty #type tile'(type, addr) {
        given:
        context.read() >> '2'
        input = """\
                INBOX
                COPYTO 1
                COPYFROM [$addr]
                """.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        thrown(EmptyTileException)

        where:
        type       | addr
        'pointer'  | 2
        'resolved' | 1
    }

    def 'debug info can be printed with DUMP'() {
        given:
        input = '''\
                INBOX
                DUMP
                '''.stripIndent()

        when:
        walker.interpret(parse(), context)

        then:
        1 * context.read() >> 23
        1 * context.dump()
    }
}
