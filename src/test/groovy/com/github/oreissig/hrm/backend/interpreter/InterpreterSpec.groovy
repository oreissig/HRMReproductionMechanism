package com.github.oreissig.hrm.backend.interpreter

import spock.lang.Unroll

import com.github.oreissig.hrm.AbstractHRMSpec

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
        0 * o.print(_)
        0 * i.read()
    }
    
    def 'I/O works'() {
        given:
        input = '''\
                inbox
                outbox
                '''.stripIndent()
        
        when:
        walker.interpret(parse())
        
        then:
        1 * i.read() >> 23
        1 * o.print(23)
    }
}
