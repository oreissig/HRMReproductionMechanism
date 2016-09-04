package com.github.oreissig.hrm.launcher

import com.github.oreissig.hrm.backend.interpreter.InterpreterContext
import groovy.transform.CompileStatic

import static com.github.oreissig.hrm.backend.interpreter.InterpreterListener.MAX_TILE

@CompileStatic
class DirectInterpreterContext implements InterpreterContext {
    static PrintWriter output = System.out.newPrintWriter()
    static BufferedReader input = System.'in'.newReader()
    static boolean LITERAL_MODE = System.properties['literal'].asBoolean()

    final Integer[] floor = new Integer[MAX_TILE]

    @Override
    Integer getAt(int pointer) {
        def value = floor[pointer]
        if (value == null && LITERAL_MODE) {
            value = pointer
            floor[pointer] = value
        }
        return value
    }

    @Override
    void putAt(int pointer, int newValue) {
        floor[pointer] = newValue
    }

    @Override
    void print(int value) {
        output.println(value as String)
    }

    @Override
    Integer read() {
        output.print '> '
        output.flush()
        try {
            return input.readLine()?.asType(int)
        } catch (IOException e) {
            return null
        }
    }

    @Override
    void dump() {
        floor.eachWithIndex { value, i ->
            if (value) {
                output.println "FLOOR TILE $i: $value"
            }
        }
    }
}
