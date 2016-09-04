package com.github.oreissig.hrm.launcher

import com.github.oreissig.hrm.backend.interpreter.InterpreterContext
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DirectInterpreterContextSpec extends Specification {

    InterpreterContext context = new DirectInterpreterContext()

    def 'floor tiles are saved'() {
        given:
        def pointer = 3
        def value = 4

        when:
        context[pointer] = value

        then:
        context[pointer] == value
    }

    def 'input works'(readline, expected) {
        given:
        BufferedReader reader = Mock()
        DirectInterpreterContext.input = reader
        PrintWriter writer = Mock()
        DirectInterpreterContext.output = writer

        when:
        def result = context.read()

        then:
        1 * writer.print("> ")
        then:
        1 * reader.readLine() >> { readline() }
        result == expected

        where:
        readline                        | expected
        { i -> "123" }                  | 123
        { i -> null }                   | null
        { i -> throw new IOException()} | null
    }

    def 'output works'() {
        given:
        PrintWriter writer = Mock()
        DirectInterpreterContext.output = writer
        def output = 123

        when:
        context.print(output)

        then:
        1 * writer.println(output.toString())
    }

    def 'literal mode initializes tiles with their index'() {
        given:
        DirectInterpreterContext.LITERAL_MODE = true
        def pointer = 23

        expect:
        pointer == context[pointer]

        cleanup:
        DirectInterpreterContext.LITERAL_MODE = false
    }
}
