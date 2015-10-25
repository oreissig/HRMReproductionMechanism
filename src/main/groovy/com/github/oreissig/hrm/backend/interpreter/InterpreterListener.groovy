package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.tree.TerminalNode

import com.github.oreissig.hrm.frontend.parser.HRMBaseListener
import com.github.oreissig.hrm.frontend.parser.HRMParser.AddContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.CopyfromContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.CopytoContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.DecrementContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.InboxContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.IncrementContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpnegContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpzeroContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.OutboxContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.SubContext

@CompileStatic
class InterpreterListener extends HRMBaseListener {
    static PrintStream output = System.out
    static InputStream input = System.'in'
    static int MAX_MEM = 9001
    
    final Integer[] mem = new Integer[MAX_MEM]
    Integer current = null
    // reuse "Jump" exception
    Jump jump = new Jump()
    
    @Override
    void enterInbox(InboxContext ctx) {
        current = input.read()
    }
    
    @Override
    void enterOutbox(OutboxContext ctx) {
        output.print current
    }
    
    @Override
    void enterCopyfrom(CopyfromContext ctx) {
        def pointer = parse(ctx.NUMBER())
        current = mem[pointer]
    }
    
    @Override
    void enterCopyto(CopytoContext ctx) {
        def pointer = parse(ctx.NUMBER())
        mem[pointer] = current
    }
    
    @Override
    void enterAdd(AddContext ctx) {
        def pointer = parse(ctx.NUMBER())
        current += mem[pointer]
    }
    
    @Override
    void enterSub(SubContext ctx) {
        def pointer = parse(ctx.NUMBER())
        current -= mem[pointer]
    }
    
    @Override
    void enterIncrement(IncrementContext ctx) {
        def pointer = parse(ctx.NUMBER())
        mem[pointer]++
    }
    
    @Override
    void enterDecrement(DecrementContext ctx) {
        def pointer = parse(ctx.NUMBER())
        mem[pointer]--
    }
    
    @Override
    void enterJump(JumpContext ctx) throws Jump {
        jump.id = ctx.ID()
        throw jump
    }
    
    @Override
    void enterJumpneg(JumpnegContext ctx) throws Jump {
        throw new UnsupportedOperationException('TODO')
    }
    
    @Override
    void enterJumpzero(JumpzeroContext ctx) throws Jump {
        throw new UnsupportedOperationException('TODO')
    }
    
    private int parse(TerminalNode node) {
        node.text.toInteger()
    }
}
