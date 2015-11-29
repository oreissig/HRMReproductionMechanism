package com.github.oreissig.hrm.frontend

import spock.lang.Unroll

import com.github.oreissig.hrm.AbstractHRMSpec
import com.github.oreissig.hrm.frontend.parser.HRMParser.AddContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.BumpdownContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.BumpupContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.CopyfromContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.CopytoContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.InboxContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpnContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpzContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.OutboxContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.SubContext

@Unroll
class ParserSpec extends AbstractHRMSpec
{
    def 'empty program parses successfully'()
    {
        given:
        input = ''
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
        tree.statement().empty
    }
    
    def 'header-like comments are ignored'()
    {
        given:
        input = '-- foo bazbazbaz'
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
        tree.statement().empty
    }
    
    def 'code comments are parsed correctly'()
    {
        given:
        input = 'COMMENT 1'
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
        tree.statement().size() == 1
        def comment = tree.statement(0).expression().comment()
        comment.NUMBER().text == '1'
    }
    
    def 'blobs are ignored'()
    {
        given:
        input = '''\
                DEFINE foo bar
                asdf!+-
                123/456;
                '''
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
        tree.statement().empty
    }
    
    def '#name statements parse successfully'(name, src, type, check)
    {
        given:
        input = src
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
        tree.statement().size() == 1
        def statement = tree.statement().first()
        def expression = statement.expression()
        def exprType = expression."$name"()
        exprType
        exprType.class == type
        check(exprType) || true // ignore return value, check happens inside closure
        
        where:
        name       | src            | type            | check
        'inbox'    | 'INBOX'        | InboxContext    | { /* nothing to do here */ }
        'outbox'   | 'OUTBOX'       | OutboxContext   | { /* nothing to do here */ }
        'copyfrom' | 'COPYFROM 123' | CopyfromContext | { assert it.NUMBER().text == '123' }
        'copyto'   | 'COPYTO 123'   | CopytoContext   | { assert it.NUMBER().text == '123' }
        'add'      | 'ADD 123'      | AddContext      | { assert it.NUMBER().text == '123' }
        'sub'      | 'SUB 123'      | SubContext      | { assert it.NUMBER().text == '123' }
        'bumpup'   | 'BUMPUP 123'   | BumpupContext   | { assert it.NUMBER().text == '123' }
        'bumpdown' | 'BUMPDN 123'   | BumpdownContext | { assert it.NUMBER().text == '123' }
        // label is the same as the human-readable one
        'jump'     | 'JUMP foo'     | JumpContext     | { assert it.ID().text == 'foo' }
        'jumpz'    | 'JUMPZ foo'    | JumpzContext    | { assert it.ID().text == 'foo' }
        'jumpn'    | 'JUMPN foo'    | JumpnContext    | { assert it.ID().text == 'foo' }
    }

    def 'multiple statements are parsed successfully'()
    {
        given:
        input = '''\
        INBOX
        
        -- barbarbarbarbar
        COPYFROM 123
        '''.stripIndent()
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
        tree.statement()*.text.size() == 2
    }

    def 'trailing new-line is optional'()
    {
        given:
        input = 'INBOX'
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
    }
}
