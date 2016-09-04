package com.github.oreissig.hrm.backend.interpreter

import com.github.oreissig.hrm.frontend.parser.HRMBaseListener
import com.github.oreissig.hrm.frontend.parser.HRMParser.AddContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.AddressContext
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
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

@CompileStatic
class InterpreterListener extends HRMBaseListener {
    static int MAX_TILE = 9001
    static int MAX_VALUE = 999
    static int MIN_VALUE = -999

    private final InterpreterContext context
    private Integer hands = null
    // reuse "Jump" exception
    private final Jump jump = new Jump()

    InterpreterListener(InterpreterContext context) {
        this.context = context
    }

    @Override
    void enterInbox(InboxContext ctx) {
        def input = context.read()
        if (input == null) {
            throw new EmptyInboxException(ctx)
        }
        setHands(input, ctx)
    }

    @Override
    void enterOutbox(OutboxContext ctx) {
        context.print(getHands(ctx))
        setHands(null, ctx)
    }

    @Override
    void enterCopyfrom(CopyfromContext ctx) {
        def pointer = resolve(ctx.address())
        setHands(getTile(ctx, pointer), ctx)
    }

    @Override
    void enterCopyto(CopytoContext ctx) {
        def pointer = resolve(ctx.address())
        context[pointer] = getHands(ctx)
    }

    @Override
    void enterAdd(AddContext ctx) {
        def pointer = resolve(ctx.address())
        setHands(getHands(ctx) + getTile(ctx, pointer), ctx)
    }

    @Override
    void enterSub(SubContext ctx) {
        def pointer = resolve(ctx.address())
        setHands(getHands(ctx) - getTile(ctx, pointer), ctx)
    }

    @Override
    void enterBumpup(BumpupContext ctx) {
        def pointer = resolve(ctx.address())
        def value = getTile(ctx, pointer)
        value++
        context[pointer] = value
        setHands(value, ctx)
    }

    @Override
    void enterBumpdown(BumpdownContext ctx) {
        def pointer = resolve(ctx.address())
        def value = getTile(ctx, pointer)
        value--
        context[pointer] = value
        setHands(value, ctx)
    }

    @Override
    void enterJump(JumpContext ctx) throws Jump {
        jump(ctx.ID())
    }

    @Override
    void enterJumpn(JumpnContext ctx) throws Jump {
        if (getHands(ctx) < 0) {
            jump(ctx.ID())
        }
    }

    @Override
    void enterJumpz(JumpzContext ctx) throws Jump {
        if (getHands(ctx) == 0) {
            jump(ctx.ID())
        }
    }

    @Override
    void enterDump(DumpContext ctx) {
        context.print 'HANDS: ' + (hands ?: 'empty')
        context.dump()
    }

    // somehow type check fails on TravisCI
    @CompileDynamic
    private int resolve(AddressContext addr) throws EmptyTileException {
        def p
        if (addr.directAddr()) {
            p = parse(addr.directAddr().NUMBER())
        } else {
            def addrTile = parse(addr.indirectAddr().NUMBER())
            p = getTile(addr.parent, addrTile)
        }
        if (p < 0 || p >= MAX_TILE)
            throw new BadTileAddressException(addr, p)
        return p
    }

    private int parse(TerminalNode node) {
        node.text.toInteger()
    }

    private void jump(TerminalNode toLabel) throws Jump {
        // this is nasty, but there's no obvious way of breaking out otherwise
        jump.label = toLabel.text
        throw jump
    }

    private int getHands(ParserRuleContext ctx) throws EmptyHandsException {
        if (hands == null)
            throw new EmptyHandsException(ctx)
        return hands
    }

    private int getTile(ParserRuleContext ctx, int pointer) throws EmptyTileException {
        def tileValue = context[pointer]
        if (tileValue == null)
            throw new EmptyTileException(ctx)
        return tileValue
    }

    private void setHands(Integer newValue, ParserRuleContext ctx = null) {
        if (newValue != null && (newValue > MAX_VALUE || newValue < MIN_VALUE))
            throw new OverflowException(ctx, newValue)
        else
            hands = newValue
    }
}
