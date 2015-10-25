package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.ParseTreeWalker

import com.github.oreissig.hrm.frontend.parser.HRMParser.ProgramContext

@CompileStatic
class InterpreterWalker {
    
    private final ParseTreeWalker walker = ParseTreeWalker.DEFAULT
    
    void interpret(ProgramContext program) {
        def labels = findLabels(program)
        
        ParseTreeListener listener = new InterpreterListener()
        // use custom walker to implement jumps
        def statements = program.statement()
        int pc = 0
        while (pc < statements.size()) {
            walker.walk(listener, statements[pc])
            // TODO implement jumps
            pc++
        }
    }
    
    private Map<String,Integer> findLabels(ProgramContext program)
    {
        Map<String,Integer> labels = [:]
        ParseTreeListener labelFinder = new LabelListener(labels)
        walker.walk(labelFinder, program)
        return labels.asImmutable()
    }
}
