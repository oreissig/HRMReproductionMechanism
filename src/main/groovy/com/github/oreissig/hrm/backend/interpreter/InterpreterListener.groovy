package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ParserRuleContext
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
    private final Jump jump = new Jump()
    
    @Override
    void enterInbox(InboxContext ctx) {
        current = input.read()
    }
    
    @Override
    void enterOutbox(OutboxContext ctx) {
        checkEmptyHands(ctx)
        output.print current
    }
    
    @Override
    void enterCopyfrom(CopyfromContext ctx) {
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        current = mem[pointer]
    }
    
    @Override
    void enterCopyto(CopytoContext ctx) {
        checkEmptyHands(ctx)
        def pointer = parse(ctx.NUMBER())
        mem[pointer] = current
    }
    
    @Override
    void enterAdd(AddContext ctx) {
        checkEmptyHands(ctx)
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        current += mem[pointer]
    }
    
    @Override
    void enterSub(SubContext ctx) {
        checkEmptyHands(ctx)
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        current -= mem[pointer]
    }
    
    @Override
    void enterIncrement(IncrementContext ctx) {
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        mem[pointer]++
    }
    
    @Override
    void enterDecrement(DecrementContext ctx) {
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        mem[pointer]--
    }
    
    @Override
    void enterJump(JumpContext ctx) throws Jump {
        jump(ctx.ID())
    }
    
    @Override
    void enterJumpneg(JumpnegContext ctx) throws Jump {
        checkEmptyHands(ctx)
        if (current < 0) {
            jump(ctx.ID())
        }
    }
    
    @Override
    void enterJumpzero(JumpzeroContext ctx) throws Jump {
        checkEmptyHands(ctx)
        if (current == 0) {
            jump(ctx.ID())
        }
    }
    
    private int parse(TerminalNode node) {
        node.text.toInteger()
    }
    
    private void jump(TerminalNode toLabel) throws Jump {
        // this is nasty, but there's no obvious way of breaking out otherwise
        jump.id = toLabel
        throw jump
    }
    
    private void checkEmptyHands(ParserRuleContext ctx) throws EmptyHandsException {
        if (current == null)
            throw new EmptyHandsException(ctx)
    }
    
    private void checkEmptyTile(ParserRuleContext ctx, int pointer) throws EmptyTileException {
        if (mem[pointer] == null)
            throw new EmptyTileException(ctx)
    }
}
