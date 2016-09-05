package com.github.oreissig.hrm

import com.github.oreissig.hrm.frontend.parser.HRMBaseListener
import com.github.oreissig.hrm.frontend.parser.HRMLexer
import com.github.oreissig.hrm.frontend.parser.HRMParser
import com.github.oreissig.hrm.frontend.parser.HRMParser.ProgramContext
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker

@TypeChecked
abstract class AbstractHRMSpec extends AntlrSpec<HRMParser>
{
	final Class<HRMParser> parserClass = HRMParser
	final Class<HRMLexer> lexerClass = HRMLexer
	
	ProgramContext parse() {
		parser.program()
	}
	
	void checkErrorFree(ParseTree tree)
	{
		ParseTreeWalker.DEFAULT.walk(new NoErrorListener(), tree)
	}
	
	@CompileStatic
	private static class NoErrorListener extends HRMBaseListener {
		@Override
		void visitErrorNode(ErrorNode node) {
			throw new Exception(node.symbol.toString())
		}
	}
}
