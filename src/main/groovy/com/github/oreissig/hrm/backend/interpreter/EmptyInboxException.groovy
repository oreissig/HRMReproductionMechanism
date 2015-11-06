package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ParserRuleContext

@CompileStatic
class EmptyInboxException extends InterpreterException {
    EmptyInboxException(ParserRuleContext operation) {
        super('The end of the inbox has been reached!', operation)
    }
}
