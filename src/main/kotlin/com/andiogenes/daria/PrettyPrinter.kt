package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern

class PrettyPrinter {
    fun print(patterns: List<Pattern>) = patterns.forEach { print(it) }

    private fun print(pattern: Pattern, depth: Int = 0): Unit = when (pattern) {
        is Pattern.Definition ->
            definePattern(pattern.name, pattern.args, pattern.body)
        is Pattern.Invocation ->
            invokePattern(pattern.name, pattern.args, depth)
        is Pattern.Value ->
            valuePattern(pattern.name, depth)
    }

    private fun valuePattern(name: String, depth: Int) {
        println("${"\t".repeat(depth)}val $name")
    }

    private fun invokePattern(name: String, args: List<Pattern>, depth: Int = 1) {
        println("${"\t".repeat(depth)}inv $name")
        args.forEach { print(it, depth + 1) }
    }

    private fun definePattern(name: String, args: List<Pattern>, body: Pattern) {
        println("def $name")
        args.forEach { print(it, 1) }
        print(body, 2)
    }
}
