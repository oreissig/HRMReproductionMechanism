package com.github.oreissig.hrm.backend.interpreter

interface InterpreterContext {

    Integer getAt(int pointer)

    void putAt(int pointer, int newValue)

    void print(int value)

    Integer read()

    void dump()
}
