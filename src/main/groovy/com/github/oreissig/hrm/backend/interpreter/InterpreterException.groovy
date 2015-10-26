package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ParserRuleContext

@CompileStatic
abstract class InterpreterException extends RuntimeException {
    final ParserRuleContext location

    InterpreterException(String msg, ParserRuleContext location) {
        super(msg)
        this.location = location
        fillInCustomStackTrace()
    }

    @Override
    Throwable fillInStackTrace() {
        // do not fill in Java stack trace
    }

    private void fillInCustomStackTrace() {
        def token = location.start
        stackTrace = [
            new StackTraceElement('human.resource.Machine', 'main',
                                  token.tokenSource.sourceName, token.line)
        ]
    }
}
