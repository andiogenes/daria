package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern
import com.andiogenes.daria.utils.Either

class Parser(private val tokens: List<Token>) {
    class ParseError(message: String) : RuntimeException(message)

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
            is Token.LineBreak -> return null
            else -> throw ParseError("Unexpected token")
        }

        val args = commonArguments()

        when (val token = peek()) {
            is Token.LineBreak, Token.EndOfFile -> {
                if (token == Token.LineBreak) advance()

                val invocationArgs = when (args) {
                    is Either.Left -> args.l
                    is Either.Right -> args.r.map { Pattern.Invocation(it, listOf()) }
                }

                return Pattern.Invocation(name, invocationArgs)
            }
            is Token.Equal -> {
                advance()

                val definitionArgs = when (args) {
                    is Either.Left -> throw ParseError("Unexpected invocations")
                    is Either.Right -> args.r
                }

                val patternInvocation = invocation()

                when (peek()) {
                    is Token.LineBreak -> advance()
                    is Token.EndOfFile -> Unit
                    else -> throw ParseError("Unexpected token")
                }

                return Pattern.Definition(name, definitionArgs, patternInvocation)
            }
            else -> throw ParseError("Unexpected token")
        }
    }

    private fun invocation(isArgument: Boolean = false): Pattern.Invocation {
        if (peek() == Token.LeftParen) {
            advance()

            val pattern = invocation()

            when (peek()) {
                is Token.RightParen -> advance()
                else -> throw ParseError("Unexpected token")
            }

            return pattern
        }

        val name = when (val token = advance()) {
            is Token.Identifier -> token.name
            else -> throw ParseError("Unexpected token")
        }

        val args = if (isArgument) {
            listOf()
        } else {
            invocationArguments()
        }

        return Pattern.Invocation(name, args)
    }

    private fun invocationArguments(): List<Pattern.Invocation> {
        val args = mutableListOf<Pattern.Invocation>()

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

    private fun commonArguments(): Either<List<Pattern.Invocation>, List<String>> {
        val args = mutableListOf<Any>()
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
                    args.add(token.name)
                    advance()
                }
                else -> throw ParseError("Unexpected token")
            }
        }

        return if (isDefinitionArgs) {
            Either.Right(args.map { it as String })
        } else {
            Either.Left(args.map {
                if (it is String) {
                    Pattern.Invocation(it, listOf())
                } else {
                    it as Pattern.Invocation
                }
            })
        }
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