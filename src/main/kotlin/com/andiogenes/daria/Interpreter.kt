package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern

class Interpreter(private val scope: Scope) {
    class RuntimeError(message: String) : RuntimeException(message)

    fun eval(patterns: List<Pattern>) = patterns.forEach { eval(it) }

    fun eval(pattern: Pattern): Pattern.Value? = when (pattern) {
        is Pattern.Definition -> definePattern(pattern.name, pattern.args, pattern.body)
        is Pattern.Invocation -> invokePattern(pattern.name, pattern.args)
        is Pattern.Value -> pattern
    }

    private fun invokePattern(
        name: String,
        args: List<Pattern>,
        localScope: Map<String, Pattern.Value>? = null
    ): Pattern.Value {
        val invokedArgs = args.map {
            when (it) {
                is Pattern.Invocation -> invokePattern(it.name, it.args)
                is Pattern.Value -> it
                else -> throw RuntimeError("Can't invoke the definition")
            }
        }
        val (pattern, body) = scope[name, invokedArgs] ?: return localScope?.get(name) ?: Pattern.Value(name)

        val ls = pattern.map { it.fullName }.zip(invokedArgs).toMap()

        return when (body) {
            is Pattern.Invocation -> invokePattern(body.name, body.args, localScope = ls)
            is Pattern.Value -> body
            else -> throw RuntimeError("Can't invoke the definition")
        }
    }

    private fun definePattern(name: String, args: List<Pattern>, body: Pattern): Pattern.Value? {
        scope[name] = args to body
        return null
    }
}