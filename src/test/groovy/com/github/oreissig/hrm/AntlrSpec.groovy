package com.github.oreissig.hrm

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.TokenStream

import spock.lang.Specification

@CompileStatic
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

	@CompileDynamic
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
}
