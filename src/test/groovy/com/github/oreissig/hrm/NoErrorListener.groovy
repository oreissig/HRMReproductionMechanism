package com.github.oreissig.hrm

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.tree.ErrorNode

import com.github.oreissig.hrm.frontend.parser.HRMBaseListener

@CompileStatic
class NoErrorListener extends HRMBaseListener {
    @Override
    void visitErrorNode(ErrorNode node) {
        throw new Exception(node.symbol.toString())
    }
}
