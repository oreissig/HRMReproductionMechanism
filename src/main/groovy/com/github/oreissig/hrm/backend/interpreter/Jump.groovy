package com.github.oreissig.hrm.backend.interpreter

class Jump extends RuntimeException {
    String label

    // avoid stack trace generation
    @Override
    Throwable fillInStackTrace() {
        this
    }
}
