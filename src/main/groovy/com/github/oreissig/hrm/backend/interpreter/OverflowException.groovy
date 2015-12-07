package com.github.oreissig.hrm.backend.interpreter

import static com.github.oreissig.hrm.backend.interpreter.InterpreterListener.MAX_VALUE
import static com.github.oreissig.hrm.backend.interpreter.InterpreterListener.MIN_VALUE
import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ParserRuleContext

@CompileStatic
class OverflowException extends InterpreterException {
    final Integer illegalValue
    
    OverflowException(ParserRuleContext operation, Integer illegalValue) {
        super("Overflow! Each data unit is restricted to values between $MIN_VALUE " +
              "and $MAX_VALUE. That should be enough for anybody.",
              operation)
        this.illegalValue = illegalValue
    }
}
