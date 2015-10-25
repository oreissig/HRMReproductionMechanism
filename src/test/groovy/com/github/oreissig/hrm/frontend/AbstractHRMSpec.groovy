package com.github.oreissig.hrm.frontend

import com.github.oreissig.hrm.frontend.parser.HRMLexer
import com.github.oreissig.hrm.frontend.parser.HRMParser

abstract class AbstractHRMSpec extends AntlrSpec<HRMParser>
{
	final Class<HRMParser> parserClass = HRMParser
	final Class<HRMLexer> lexerClass = HRMLexer
}
