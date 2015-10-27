package com.github.oreissig.hrm.launcher

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ANTLRFileStream
import org.antlr.v4.runtime.CommonTokenStream

import com.github.oreissig.hrm.backend.interpreter.InterpreterWalker
import com.github.oreissig.hrm.frontend.parser.HRMLexer
import com.github.oreissig.hrm.frontend.parser.HRMParser


def cli = new CliBuilder(usage:'HRMReproductionMechanism [-options] sourceFile')

cli.with {
    // TODO activate once there's a second backend
    //i 'runs the interpreter (default)'
    //l 'generates LLVM IR assembly'
}

def options = cli.parse(args)
def file = options.arguments()[0]

if (!file) {
    cli.usage()
    System.exit(1)
} else {
    interpret(file)
}

System.exit(0)


@CompileStatic
void interpret(String file) {
    def chars = new ANTLRFileStream(file)
    def lexer = new HRMLexer(chars)
    def tokens = new CommonTokenStream(lexer)
    def parser = new HRMParser(tokens)
    def tree = parser.program()
    
    new InterpreterWalker().interpret(tree)
}
