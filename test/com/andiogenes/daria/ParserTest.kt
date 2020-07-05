package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.function.Executable

internal class ParserTest {

    @Test
    fun simple() {
        val source = "and true true = true"

        val expected = listOf(
            Pattern.Definition(
                "and",
                listOf("true", "true"),
                Pattern.Invocation("true", listOf())
            )
        )
        val got = Parser(Lexer(source).lex()).parse()

        assertEquals(expected, got)
    }

    @Test
    fun `Line Break`() {
        val source = "and true true = true\n"

        val expected = listOf(
            Pattern.Definition(
                "and",
                listOf("true", "true"),
                Pattern.Invocation("true", listOf())
            )
        )
        val got = Parser(Lexer(source).lex()).parse()
        assertEquals(expected, got)
    }

    @Test
    fun `Line Breaks`() {
        val source = "\n\n\nand true true = true\n\n\n\n"

        val expected = listOf(
            Pattern.Definition(
                "and",
                listOf("true", "true"),
                Pattern.Invocation("true", listOf())
            )
        )
        val got = Parser(Lexer(source).lex()).parse()
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
            Pattern.Definition(
                "and",
                listOf("true", "true"),
                Pattern.Invocation("true", listOf())
            ),
            Pattern.Definition(
                "and",
                listOf("_", "_"),
                Pattern.Invocation("false", listOf())
            ),
            Pattern.Definition(
                "or",
                listOf("false", "false"),
                Pattern.Invocation("false", listOf())
            ),
            Pattern.Definition(
                "or",
                listOf("_", "_"),
                Pattern.Invocation("true", listOf())
            )
        )
        val got = Parser(Lexer(source).lex()).parse()
        assertEquals(expected, got)
    }

    @Test
    fun `Parenthesised expressions`() {
        val source = """
            fac 0 = 1
            fac x = * x (fac (- x 1))
        """.trimIndent()

        val expected = listOf(
            Pattern.Definition(
                "fac",
                listOf("0"),
                Pattern.Invocation("1", listOf())
            ),
            Pattern.Definition(
                "fac",
                listOf("x"),
                Pattern.Invocation(
                    "*",
                    listOf(
                        Pattern.Invocation("x", listOf()),
                        Pattern.Invocation(
                            "fac",
                            listOf(
                                Pattern.Invocation(
                                    "-",
                                    listOf(
                                        Pattern.Invocation("x", listOf()),
                                        Pattern.Invocation("1", listOf())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        val got = Parser(Lexer(source).lex()).parse()
        assertEquals(expected, got)
    }

    @Test
    fun `Empty Source`() {
        val expected = listOf<Pattern>()
        val got = Parser(Lexer("").lex()).parse()
        assertEquals(expected, got)
    }

    @Test
    fun `Deep parentheses`() {
        val source = "deep = ((((((x y))))))"

        val expected = listOf(
            Pattern.Definition(
                "deep",
                listOf(),
                Pattern.Invocation(
                    "x",
                    listOf(
                        Pattern.Invocation(
                            "y",
                            listOf()
                        )
                    )
                )
            )
        )
        val got = Parser(Lexer(source).lex()).parse()
        assertEquals(expected, got)
    }

    @Test
    fun `Unmatched parentheses 1`() {
        val source = "deep = (((((x y))))))"
        assertThrows(Parser.ParseError::class.java) {
            Parser(Lexer(source).lex()).parse()
        }
    }

    @Test
    fun `Unmatched parentheses 2`() {
        val source = "deep = ((((((x y)))))"
        assertThrows(Parser.ParseError::class.java) {
            Parser(Lexer(source).lex()).parse()
        }
    }

    @Test
    fun `Unmatched parentheses 3`() {
        val source = "che = prost)"
        assertThrows(Parser.ParseError::class.java) {
            Parser(Lexer(source).lex()).parse()
        }
    }

    @Test
    fun `Unmatched parentheses 4`() {
        val source = """
            fac 0 = 1
            fac x = * x (fac (- x 1)
        """.trimIndent()
        assertThrows(Parser.ParseError::class.java) {
            Parser(Lexer(source).lex()).parse()
        }
    }

    @Test
    fun `Unexpected token at start`() {
        val suffix = "= ="
        assertAll(
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(Lexer("= $suffix").lex()).parse()
                }
            },
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(Lexer("( $suffix").lex()).parse()
                }
            },
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(Lexer(") $suffix").lex()).parse()
                }
            }
        )
    }

    @Test
    fun `Unexpected invocations`() {
        val source = "def (foo) = bar"
        assertThrows(Parser.ParseError::class.java) {
            Parser(Lexer(source).lex()).parse()
        }
    }

    @Test
    fun `Unexpected token at the end of invocation`() {
        val prefix = "f x y"
        assertAll(
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(Lexer("$prefix)").lex()).parse()
                }
            },
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(Lexer("$prefix(").lex()).parse()
                }
            }
        )
    }

    @Test
    fun `Unexpected token at the end of definition`() {
        val prefix = "g y x = f x y"
        assertAll(
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(Lexer("$prefix)").lex()).parse()
                }
            },
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(Lexer("$prefix(").lex()).parse()
                }
            }
        )
    }

    @Test
    fun `Empty definition body`() {
        val source = "empty body ="
        assertThrows(Parser.ParseError::class.java) {
            Parser(Lexer(source).lex()).parse()
        }
    }
}