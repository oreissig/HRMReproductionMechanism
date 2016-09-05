package com.github.oreissig.hrm.backend.jsr223

import com.github.oreissig.hrm.backend.interpreter.InterpreterContext
import groovy.transform.CompileStatic

import javax.script.SimpleScriptContext

@CompileStatic
class HRMScriptContext extends SimpleScriptContext implements InterpreterContext {
    @Override
    Integer getAt(int pointer) {
        getAttribute(pointer as String) as int
    }

    @Override
    void putAt(int pointer, int newValue) {
        setAttribute(pointer as String, newValue, ENGINE_SCOPE)
    }

    @Override
    void print(int value) {
        writer.write(value)
    }

    @Override
    Integer read() {
        reader.read()
    }

    @Override
    void dump() {
        throw new UnsupportedOperationException("TODO")
    }
}
