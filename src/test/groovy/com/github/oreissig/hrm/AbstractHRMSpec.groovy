package com.github.oreissig.hrm

import groovy.transform.CompileStatic

import com.github.oreissig.hrm.frontend.parser.HRMLexer
import com.github.oreissig.hrm.frontend.parser.HRMParser
import com.github.oreissig.hrm.frontend.parser.HRMParser.ProgramContext

@CompileStatic
abstract class AbstractHRMSpec extends AntlrSpec<HRMParser>
{
	final Class<HRMParser> parserClass = HRMParser
	final Class<HRMLexer> lexerClass = HRMLexer
	
	ProgramContext parse() {
		parser.program()
	}
}
