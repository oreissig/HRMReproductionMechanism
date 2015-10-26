package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ParserRuleContext

@CompileStatic
class EmptyTileException extends InterpreterException {
    EmptyTileException(ParserRuleContext operation) {
        super("You can't '${operation.children*.text.join(' ')}' with an empty tile on the floor!",
              operation)
    }
}
