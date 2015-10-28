package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

@CompileStatic
class Jump extends RuntimeException {
    String label

    // avoid stack trace generation
    @Override
    Throwable fillInStackTrace() {
        this
    }
}
