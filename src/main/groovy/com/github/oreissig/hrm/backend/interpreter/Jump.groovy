package com.github.oreissig.hrm.backend.interpreter

import org.antlr.v4.runtime.tree.TerminalNode

class Jump extends RuntimeException {
    TerminalNode id

    // avoid stack trace generation
    @Override
    Throwable fillInStackTrace() {
        this
    }
}
