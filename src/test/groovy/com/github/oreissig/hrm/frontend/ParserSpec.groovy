package com.github.oreissig.hrm.frontend

import spock.lang.Stepwise
import spock.lang.Unroll

import com.github.oreissig.hrm.AbstractHRMSpec
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

@Stepwise
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
    
    def 'trailing new-line is optional'()
    {
        given:
        input = 'INBOX'
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
    }
    
    def '#name statements parse successfully'(name, src, type)
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
        
        where:
        name       | src            | type
        'inbox'    | 'INBOX'        | InboxContext
        'outbox'   | 'OUTBOX'       | OutboxContext
        'copyfrom' | 'COPYFROM 123' | CopyfromContext
        'copyto'   | 'COPYTO 123'   | CopytoContext
        'add'      | 'ADD 123'      | AddContext
        'sub'      | 'SUB 123'      | SubContext
        'bumpup'   | 'BUMPUP 123'   | BumpupContext
        'bumpdown' | 'BUMPDN 123'   | BumpdownContext
        'jump'     | 'JUMP foo'     | JumpContext
        'jumpz'    | 'JUMPZ foo'    | JumpzContext
        'jumpn'    | 'JUMPN foo'    | JumpnContext
        'dump'     | 'DUMP'         | DumpContext
    }
    
    def 'comment statements are parsed correctly'()
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
    
    def '#name statements can use a #type address'(name,type,src) {
        given:
        input = src
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
        def op = tree.statement().first().expression()."$name"()
        def addr = op.address()
        addr."${type}Addr"()?.NUMBER()?.text == '123'
        
        where:
        name       | type | src
        'add'      | 'direct'   | 'ADD 123'
        'add'      | 'indirect' | 'ADD [123]'
        'bumpdown' | 'direct'   | 'BUMPDN 123'
        'bumpdown' | 'indirect' | 'BUMPDN [123]'
        'bumpup'   | 'direct'   | 'BUMPUP 123'
        'bumpup'   | 'indirect' | 'BUMPUP [123]'
        'copyfrom' | 'direct'   | 'COPYFROM 123'
        'copyfrom' | 'indirect' | 'COPYFROM [123]'
        'copyto'   | 'direct'   | 'COPYTO 123'
        'copyto'   | 'indirect' | 'COPYTO [123]'
        'sub'      | 'direct'   | 'SUB 123'
        'sub'      | 'indirect' | 'SUB [123]'
    }
    
    def 'jump targets are parsed for #name'(name) {
        given:
        def label = 'foo'
        input = "${name.toUpperCase()} $label"
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
        def jump = tree.statement().first().expression()."$name"()
        jump.ID().text == label
        
        where:
        name << ['jump','jumpn','jumpz']
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
}
