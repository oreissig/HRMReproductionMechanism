package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ParserRuleContext

@CompileStatic
class EmptyHandsException extends InterpreterException {
    EmptyHandsException(ParserRuleContext operation) {
        super("You can't '${operation.children*.text.join(' ')}' with empty hands!",
              operation)
    }
}
