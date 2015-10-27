package com.github.oreissig.hrm.launcher

import java.io.BufferedReader;
import java.io.PrintWriter;

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
        InterpreterListener.input = i
        InterpreterListener.output = o
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
               inbox
               copyto 1
               bump+ 1
               outbox'''
        
        when:
        Main.main(src.absolutePath)
        
        then:
        1 * i.readLine() >> '23'
        1 * o.println('24')
        0 * _
    }
}
