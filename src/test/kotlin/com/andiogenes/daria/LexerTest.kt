package com.andiogenes.daria

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class LexerTest {

    @Test
    fun simple() {
        val expected = listOf(
            Token.Identifier("and"),
            Token.Identifier("true"),
            Token.Identifier("true"),
            Token.Equal,
            Token.Identifier("true"),
            Token.EndOfFile
        )
        val got = Lexer("and true true = true").lex()

        assertEquals(expected, got)
    }

    @Test
    fun `Line Break`() {
        val source = "and true true = true\n"

        val expected = listOf(
            Token.Identifier("and"),
            Token.Identifier("true"),
            Token.Identifier("true"),
            Token.Equal,
            Token.Identifier("true"),
            Token.LineBreak,
            Token.EndOfFile
        )
        val got = Lexer(source).lex()

        assertEquals(expected, got)
    }

    @Test
    fun `Complex Source File`() {
        val source = """
            and true true = true
            and _ _ = false

            or false false = false
            or _ _ = true
        """.trimIndent()

        val expected = listOf(
            Token.Identifier("and"),
            Token.Identifier("true"),
            Token.Identifier("true"),
            Token.Equal,
            Token.Identifier("true"),
            Token.LineBreak,
            Token.Identifier("and"),
            Token.Identifier("_"),
            Token.Identifier("_"),
            Token.Equal,
            Token.Identifier("false"),
            Token.LineBreak,
            Token.LineBreak,
            Token.Identifier("or"),
            Token.Identifier("false"),
            Token.Identifier("false"),
            Token.Equal,
            Token.Identifier("false"),
            Token.LineBreak,
            Token.Identifier("or"),
            Token.Identifier("_"),
            Token.Identifier("_"),
            Token.Equal,
            Token.Identifier("true"),
            Token.EndOfFile
        )
        val got = Lexer(source).lex()
        assertEquals(expected, got)
    }

    @Test
    fun comments() {
        val source = """
            ; this is a comment
            ;; this is also a comment
            ; or false false = false
            ;; and true true = true
            and true true = true ; hey!
            ;; Hey!
        """.trimIndent()

        val expected = listOf(
            Token.LineBreak,
            Token.LineBreak,
            Token.LineBreak,
            Token.LineBreak,
            Token.Identifier("and"),
            Token.Identifier("true"),
            Token.Identifier("true"),
            Token.Equal,
            Token.Identifier("true"),
            Token.LineBreak,
            Token.EndOfFile
        )
        val got = Lexer(source).lex()
        assertEquals(expected, got)
    }

    @Test
    fun `Parenthesised expressions`() {
        val source = """
            fac 0 = 1
            fac x = * x (fac (- x 1))
        """.trimIndent()

        val expected = listOf(
            Token.Identifier("fac"),
            Token.Identifier("0"),
            Token.Equal,
            Token.Identifier("1"),
            Token.LineBreak,
            Token.Identifier("fac"),
            Token.Identifier("x"),
            Token.Equal,
            Token.Identifier("*"),
            Token.Identifier("x"),
            Token.LeftParen,
            Token.Identifier("fac"),
            Token.LeftParen,
            Token.Identifier("-"),
            Token.Identifier("x"),
            Token.Identifier("1"),
            Token.RightParen,
            Token.RightParen,
            Token.EndOfFile
        )
        val got = Lexer(source).lex()
        assertEquals(expected, got)
    }

    @Test
    fun `Empty Source`() {
        val expected = listOf(Token.EndOfFile)
        val got = Lexer("").lex()
        assertEquals(expected, got)
    }
}