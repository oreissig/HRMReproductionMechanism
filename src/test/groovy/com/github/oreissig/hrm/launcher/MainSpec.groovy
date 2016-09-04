package com.github.oreissig.hrm.launcher

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

import com.github.oreissig.hrm.backend.interpreter.InterpreterListener

class MainSpec extends Specification {
    BufferedReader i = Mock()
    PrintWriter o = Mock()
    
    @Rule
    TemporaryFolder dir = new TemporaryFolder()
    
    def setup() {
        DirectInterpreterContext.input = i
        DirectInterpreterContext.output = o
    }
    
    def 'empty args print usage'() {
        when:
        Main.main()
        
        then:
        // TODO how to test System.out
        noExceptionThrown()
    }
    
    def 'supplying a source file executes the interpreter'() {
        given:
        def src = dir.newFile()
        src << '''\
               INBOX
               COPYTO 1
               BUMPUP 1
               OUTBOX'''
        
        when:
        Main.main(src.absolutePath)
        
        then:
        1 * i.readLine() >> '23'
        1 * o.println('24')
    }
}
