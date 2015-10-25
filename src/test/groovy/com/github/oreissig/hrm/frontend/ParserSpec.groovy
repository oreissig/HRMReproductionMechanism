package com.github.oreissig.hrm.frontend

class ParserSpec extends AbstractHRMSpec
{
	def "empty program parses successfully"()
	{
		given:
		input = ''
		
		when:
		parser.program()
		
		then:
		noExceptionThrown()
	}
}
