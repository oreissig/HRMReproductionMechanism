package com.github.oreissig.hrm.frontend

import spock.lang.Unroll

import com.github.oreissig.hrm.AbstractHRMSpec
import com.github.oreissig.hrm.frontend.parser.HRMParser.AddContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.CopyfromContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.CopytoContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.DecrementContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.InboxContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.IncrementContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpnegContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.JumpzeroContext
import com.github.oreissig.hrm.frontend.parser.HRMParser.LabelContext
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
    
    def 'comments parse successfully'()
    {
        given:
        input = '... foo bar'
        
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
        name        | src                    | type             | check
        'inbox'     | 'inbox'                | InboxContext     | { /* nothing to do here */ }
        'outbox'    | 'outbox'               | OutboxContext    | { /* nothing to do here */ }
        'copyfrom'  | 'copyfrom 123'         | CopyfromContext  | { assert it.NUMBER().text == '123' }
        'copyto'    | 'copyto 123'           | CopytoContext    | { assert it.NUMBER().text == '123' }
        'add'       | 'add 123'              | AddContext       | { assert it.NUMBER().text == '123' }
        'sub'       | 'sub 123'              | SubContext       | { assert it.NUMBER().text == '123' }
        'increment' | 'bump+ 123'            | IncrementContext | { assert it.NUMBER().text == '123' }
        'decrement' | 'bump- 123'            | DecrementContext | { assert it.NUMBER().text == '123' }
        'label'     | ':thelabel'            | LabelContext     | { assert it.ID().text == 'thelabel' }
        'jump'      | 'jump foo'             | JumpContext      | { assert it.ID().text == 'foo' }
        'jumpzero'  | 'jump if zero foo'     | JumpzeroContext  | { assert it.ID().text == 'foo' }
        'jumpneg'   | 'jump if negative foo' | JumpnegContext   | { assert it.ID().text == 'foo' }
    }

    def 'multiple statements are parsed successfully'()
    {
        given:
        input = '''\
        inbox
        ... barbarbarbarbar
        copyfrom 123
        '''.stripIndent()
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
        tree.statement().size() == 2
    }

    def 'trailing new-line is optional'()
    {
        given:
        input = 'inbox'
        
        when:
        def tree = parse()
        
        then:
        checkErrorFree(tree)
    }
}
