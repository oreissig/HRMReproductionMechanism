package com.github.oreissig.hrm.backend.jsr223

import com.github.oreissig.hrm.backend.interpreter.InterpreterContext
import com.github.oreissig.hrm.backend.interpreter.InterpreterException
import com.github.oreissig.hrm.backend.interpreter.InterpreterWalker
import com.github.oreissig.hrm.frontend.parser.HRMLexer
import com.github.oreissig.hrm.frontend.parser.HRMParser
import groovy.transform.CompileStatic
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream

import javax.script.AbstractScriptEngine
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngineFactory
import javax.script.ScriptException
import javax.script.SimpleBindings

@CompileStatic
class HRMScriptEngine extends AbstractScriptEngine {
    final ScriptEngineFactory factory

    HRMScriptEngine(ScriptEngineFactory factory) {
        this.factory = factory
        context = new HRMScriptContext();
    }

    @Override
    Object eval(String script, ScriptContext context) throws ScriptException {
        doEval(new ANTLRInputStream(script), context)
    }

    @Override
    Object eval(Reader reader, ScriptContext context) throws ScriptException {
        doEval(new ANTLRInputStream(reader), context)
    }

    private Object doEval(ANTLRInputStream source, ScriptContext context) {
        def lexer = new HRMLexer(source)
        def tokens = new CommonTokenStream(lexer)
        def parser = new HRMParser(tokens)
        def tree = parser.program()

        try {
            new InterpreterWalker().interpret(tree, (InterpreterContext) context)
        } catch (InterpreterException e) {
            def token = e.location.start
            throw new ScriptException(e.message, token.tokenSource.sourceName, token.line)
        }
    }

    @Override
    Bindings createBindings() {
        new SimpleBindings()
    }

    @Override
    protected ScriptContext getScriptContext(Bindings nn) {
        def ctxt = new HRMScriptContext()

        def gs = getBindings(ScriptContext.GLOBAL_SCOPE)
        if (gs)
            ctxt.setBindings(gs, ScriptContext.GLOBAL_SCOPE)

        if (nn)
            ctxt.setBindings(nn, ScriptContext.ENGINE_SCOPE)
        else
            throw new NullPointerException("Engine scope Bindings may not be null.")

        ctxt.reader = context.reader
        ctxt.writer = context.writer
        ctxt.errorWriter = context.errorWriter

        return ctxt
    }
}
