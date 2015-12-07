package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ParserRuleContext

@CompileStatic
class BadTileAddressException extends InterpreterException {
    final int badAddress
    
    BadTileAddressException(ParserRuleContext operation, int badAddress) {
        super("Bad tile address! Tile with address $badAddress does not exist! " +
              "Where do you think you're going?",
              operation)
        this.badAddress = badAddress
    }
}
