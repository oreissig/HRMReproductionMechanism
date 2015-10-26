package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ParserRuleContext

@CompileStatic
class UnknownJumpLabelException extends InterpreterException {
    UnknownJumpLabelException(ParserRuleContext operation, String label) {
        super("You can't '${operation.children*.text.join(' ')}' because $label is not known!",
              operation)
    }
}
