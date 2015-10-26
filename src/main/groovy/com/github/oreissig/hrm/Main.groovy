package com.github.oreissig.hrm

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ANTLRFileStream
import org.antlr.v4.runtime.CommonTokenStream

import com.github.oreissig.hrm.backend.interpreter.InterpreterWalker
import com.github.oreissig.hrm.frontend.parser.HRMLexer
import com.github.oreissig.hrm.frontend.parser.HRMParser


def cli = new CliBuilder(usage:'java -jar hrm.jar [-options] sourceFile')

cli.with {
    i 'runs the interpreter'
    //l 'generates LLVM IR assembly'
}

def options = cli.parse(args)
def file = options.arguments()[0]

if (options.i) {
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
