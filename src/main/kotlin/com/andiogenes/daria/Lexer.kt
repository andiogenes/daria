package com.andiogenes.daria

class Lexer(private val source: String) {
    private val tokens = arrayListOf<Token>()

    private var start: Int = 0
    private var current: Int = 0

    fun lex(): List<Token> {
        while (!isAtEnd()) {
            start = current
            acceptToken()
        }

        addToken(Token.EndOfFile)
        return tokens
    }

    private fun acceptToken() {
        when (advance()) {
            '(' -> addToken(Token.LeftParen)
            ')' -> addToken(Token.RightParen)
            '=' -> addToken(Token.Equal)
            '\n' -> addToken(Token.LineBreak)
            ';' -> eliminateComment()
            ' ', '\r', '\t' -> doNothing()
            else -> addIdentifier()
        }
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun addToken(token: Token) =
        tokens.add(token)

    private fun addIdentifier() {
        val forbiddenSymbols = "()=; \r\t\n"

        while (peek() !in forbiddenSymbols && !isAtEnd()) {
            advance()
        }

        val name = source.substring(start, current)

        addToken(Token.Identifier(name))
    }

    private fun eliminateComment() {
        while (peek() != '\n' && !isAtEnd()) {
            advance()
        }
    }

    private fun doNothing() = Unit

    private fun isAtEnd(): Boolean =
        current >= source.length
}