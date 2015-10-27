package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

import com.github.oreissig.hrm.frontend.parser.HRMBaseListener
import com.github.oreissig.hrm.frontend.parser.HRMParser

@CompileStatic
class LabelListener extends HRMBaseListener {
    private final Map<String,Integer> labels

    LabelListener(Map<String,Integer> labels) {
        this.labels = labels
    }

    // somehow type check fails on TravisCI
    @CompileStatic(TypeCheckingMode.SKIP)
    @Override
    void enterLabel(HRMParser.LabelContext ctx) {
        def label = ctx.ID().text
        def statement = ctx.parent.parent
        def program = statement.parent
        def index = program.children.indexOf(statement)
        labels[label] = index
    }
}