package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern

class Interpreter(private val scope: Scope) {
    class RuntimeError(message: String) : RuntimeException(message)

    fun eval(patterns: List<Pattern>) = patterns.forEach { eval(it) }

    fun eval(pattern: Pattern): Any = when (pattern) {
        is Pattern.Definition ->
            definePattern(pattern.name, pattern.args, pattern.body)
        is Pattern.Invocation ->
            invokePattern(pattern.name, pattern.args)
    }

    private fun invokePattern(
        name: String,
        args: List<Pattern.Invocation>,
        localScope: Map<String, String>? = null
    ): String {
        val invokedArgs = args.map { invokePattern(it.name, it.args) }
        val (pattern, body) = scope[name, invokedArgs] ?: return localScope?.get(name) ?: name

        val ls = pattern.zip(invokedArgs).toMap()

        return invokePattern(body.name, body.args, localScope = ls)
    }

    private fun definePattern(name: String, args: List<String>, body: Pattern.Invocation) {
        scope[name] = args to body
    }
}