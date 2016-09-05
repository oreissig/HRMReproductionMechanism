package com.github.oreissig.hrm.backend.jsr223

import groovy.transform.CompileStatic

import javax.script.ScriptEngine
import javax.script.ScriptEngineFactory

@CompileStatic
class HRMScriptEngineFactory implements ScriptEngineFactory {

    final String engineName = 'HRMReproductionMechanism'
    final String engineVersion = '1.0.0'

    final List<String> extensions = ['hrm'].asImmutable()
    final List<String> mimeTypes = [].asImmutable()
    final List<String> names = ['HRM', 'hrm'].asImmutable()

    final String languageName = 'Human Resource Machine'
    final String languageVersion = '1.0.0'

    @Override
    Object getParameter(String key) {
        switch (key) {
            case ScriptEngine.ENGINE:
                return engineName
            case ScriptEngine.ENGINE_VERSION:
                return engineVersion
            case ScriptEngine.NAME:
                return 'hrm';
            case ScriptEngine.LANGUAGE:
                return languageName
            case ScriptEngine.LANGUAGE_VERSION:
                return languageVersion
            default:
                return null
        }
    }

    @Override
    String getMethodCallSyntax(String obj, String m, String... args) {
        throw new UnsupportedOperationException("HRM cannot call Java objects")
    }

    @Override
    String getOutputStatement(String toDisplay) {
        "OUTBOX"
    }

    @Override
    String getProgram(String... statements) {
        statements.join('\n')
    }

    @Override
    ScriptEngine getScriptEngine() {
        return new HRMScriptEngine(this)
    }
}
