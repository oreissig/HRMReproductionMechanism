package com.github.oreissig.hrm

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker

import spock.lang.Specification

abstract class AntlrSpec<P extends Parser> extends Specification {
	/**
	 * @return Lexer class to instantiate
	 */
	abstract Class<? extends Lexer> getLexerClass()

	/**
	 * @return Parser class to instantiate
	 */
	abstract Class<P> getParserClass()

	/**
	 * The input to feed the parser with.
	 * May be {@link InputStream}, {@link Reader} or {@link String}.
	 */
	def input

	CharStream getCharStream() {
		new ANTLRInputStream(input)
	}

	Lexer getLexer() {
		lexerClass.newInstance(charStream)
	}

	TokenStream getTokenStream() {
		new CommonTokenStream(lexer)
	}

	/**
	 * @return fully initialized parser
	 */
	P getParser() {
		parserClass.newInstance(tokenStream)
	}

	void checkErrorFree(ParseTree tree)
	{
		ParseTreeWalker.DEFAULT.walk(new NoErrorListener(), tree)
	}
}
