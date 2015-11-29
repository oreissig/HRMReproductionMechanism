package com.github.oreissig.hrm.backend.interpreter

import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

import com.github.oreissig.hrm.frontend.parser.HRMBaseListener
import com.github.oreissig.hrm.frontend.parser.HRMParser.AddContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.BumpdownContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.BumpupContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.CopyfromContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.CopytoContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.DumpContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.InboxContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpnContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpzContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.OutboxContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.SubContext

@CompileStatic
class InterpreterListener extends HRMBaseListener {
    static PrintWriter output = System.out.newPrintWriter()
    static BufferedReader input = System.'in'.newReader()
    static int MAX_TILE = 9001
    static boolean LITERAL_MODE = System.properties['literal'].asBoolean()
    
    final Integer[] floor = new Integer[MAX_TILE]
    Integer hands = null
    // reuse "Jump" exception
    private final Jump jump = new Jump()
    
    @Override
    void enterInbox(InboxContext ctx) {
        output.print '> '
        output.flush()
        def raw = input.readLine()
        if (raw == null || raw.empty)
            throw new EmptyInboxException(ctx)
        hands = raw as int
    }
    
    @Override
    void enterOutbox(OutboxContext ctx) {
        checkEmptyHands(ctx)
        output.println(hands as String)
        hands = null
    }
    
    @Override
    void enterCopyfrom(CopyfromContext ctx) {
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        hands = floor[pointer]
    }
    
    @Override
    void enterCopyto(CopytoContext ctx) {
        checkEmptyHands(ctx)
        def pointer = parse(ctx.NUMBER())
        floor[pointer] = hands
    }
    
    @Override
    void enterAdd(AddContext ctx) {
        checkEmptyHands(ctx)
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        hands += floor[pointer]
    }
    
    @Override
    void enterSub(SubContext ctx) {
        checkEmptyHands(ctx)
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        hands -= floor[pointer]
    }
    
    @Override
    void enterBumpup(BumpupContext ctx) {
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        def value = floor[pointer]
        value++
        floor[pointer] = value
        hands = value
    }
    
    @Override
    void enterBumpdown(BumpdownContext ctx) {
        def pointer = parse(ctx.NUMBER())
        checkEmptyTile(ctx, pointer)
        def value = floor[pointer]
        value--
        floor[pointer] = value
        hands = value
    }
    
    @Override
    void enterJump(JumpContext ctx) throws Jump {
        jump(ctx.ID())
    }
    
    @Override
    void enterJumpn(JumpnContext ctx) throws Jump {
        checkEmptyHands(ctx)
        if (hands < 0) {
            jump(ctx.ID())
        }
    }
    
    @Override
    void enterJumpz(JumpzContext ctx) throws Jump {
        checkEmptyHands(ctx)
        if (hands == 0) {
            jump(ctx.ID())
        }
    }
    
    @Override
    void enterDump(DumpContext ctx) {
        output.println 'HANDS: ' + (hands ?: 'empty')
        floor.eachWithIndex { value, i ->
            if (value) {
                output.println "FLOOR TILE $i: $value"
            }
        }
    }
    
    private int parse(TerminalNode node) {
        node.text.toInteger()
    }
    
    private void jump(TerminalNode toLabel) throws Jump {
        // this is nasty, but there's no obvious way of breaking out otherwise
        jump.label = toLabel.text
        throw jump
    }
    
    private void checkEmptyHands(ParserRuleContext ctx) throws EmptyHandsException {
        if (hands == null)
            throw new EmptyHandsException(ctx)
    }
    
    private void checkEmptyTile(ParserRuleContext ctx, int pointer) throws EmptyTileException {
        if (floor[pointer] == null) {
            if (LITERAL_MODE)
                floor[pointer] = pointer
            else
                throw new EmptyTileException(ctx)
        }
    }
}
