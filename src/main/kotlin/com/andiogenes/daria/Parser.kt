package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern

class Parser(private val tokens: List<Token>) {
    class ParseError(message: String) : RuntimeException(message) {
        companion object {
            fun unexpectedToken(): Nothing = throw ParseError("Unexpected token")
        }
    }

    private var current: Int = 0

    fun parse(): List<Pattern> {
        val patterns = mutableListOf<Pattern>()
        while (!isAtEnd()) {
            val pattern = definitionOrInvocation()

            if (pattern != null) patterns.add(pattern)
        }

        return patterns
    }

    private fun definitionOrInvocation(): Pattern? {
        val name = when (val token = advance()) {
            is Token.Identifier -> token.name
            is Token.Value -> return Pattern.Value(token.name)
            is Token.LineBreak -> return null
            else -> ParseError.unexpectedToken()
        }

        val (args, isDefinitionArgs) = commonArguments()

        when (val token = peek()) {
            is Token.LineBreak, Token.EndOfFile -> {
                if (token == Token.LineBreak) advance()

                return Pattern.Invocation(name, args)
            }
            is Token.Equal -> {
                advance()

                if (!isDefinitionArgs && args.any { it is Pattern.Invocation && it.args.isNotEmpty() }) throw ParseError("Unexpected invocations")

                val patternInvocation = invocation()

                when (peek()) {
                    is Token.LineBreak -> advance()
                    is Token.EndOfFile -> Unit
                    else -> ParseError.unexpectedToken()
                }

                return Pattern.Definition(name, args, patternInvocation)
            }
            else -> ParseError.unexpectedToken()
        }
    }

    private fun invocation(isArgument: Boolean = false): Pattern {
        if (peek() == Token.LeftParen) {
            advance()

            val pattern = invocation()

            when (peek()) {
                is Token.RightParen -> advance()
                else -> ParseError.unexpectedToken()
            }

            return pattern
        }

        return when (val token = advance()) {
            is Token.Identifier -> {
                val name = token.name
                val args = if (isArgument) listOf() else invocationArguments()
                Pattern.Invocation(name, args)
            }
            is Token.Value -> Pattern.Value(token.name)
            else -> ParseError.unexpectedToken()
        }
    }

    private fun invocationArguments(): List<Pattern> {
        val args = mutableListOf<Pattern>()

        loop@ while (true) {
            when (peek()) {
                is Token.LineBreak, Token.EndOfFile -> break@loop
                is Token.RightParen -> break@loop
                else -> Unit
            }

            args.add(invocation(isArgument = true))
        }

        return args
    }

    private fun commonArguments(): Pair<List<Pattern>, Boolean> {
        val args = mutableListOf<Pattern>()
        var isDefinitionArgs = true

        loop@ while (true) {
            when (val token = peek()) {
                is Token.LineBreak, Token.EndOfFile, Token.Equal -> {
                    break@loop
                }
                is Token.LeftParen -> {
                    isDefinitionArgs = false
                    args.add(invocation())
                }
                is Token.Identifier -> {
                    isDefinitionArgs = false
                    args.add(Pattern.Invocation(token.name, listOf()))
                    advance()
                }
                is Token.Value -> {
                    args.add(Pattern.Value(token.name))
                    advance()
                }
                else -> ParseError.unexpectedToken()
            }
        }

        return args to isDefinitionArgs
    }

    private fun isAtEnd() =
        peek() == Token.EndOfFile

    private fun advance(): Token {
        current++
        return tokens[current - 1]
    }

    private fun peek() =
        tokens[current]
}