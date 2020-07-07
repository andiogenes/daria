package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern

class PrettyPrinter {
    fun eval(patterns: List<Pattern>) = patterns.forEach { eval(it) }

    private fun eval(pattern: Pattern): Unit = when (pattern) {
        is Pattern.Definition ->
            definePattern(pattern.name, pattern.args, pattern.body)
        is Pattern.Invocation ->
            invokePattern(pattern.name, pattern.args, depth = 0)
    }

    private fun invokePattern(name: String, args: List<Pattern.Invocation>, depth: Int = 1) {
        println("${"\t".repeat(depth)}inv $name")
        args.forEach { invokePattern(it.name, it.args, depth + 1) }
    }

    private fun definePattern(name: String, args: List<String>, body: Pattern.Invocation) {
        println("def $name: ${args.joinToString()}")
        invokePattern(body.name, body.args)
    }
}