package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.function.Executable

internal class ParserTest {

    @Test
    fun simple() {
        // and :true :true = :true
        val source = listOf(
            Token.Identifier("and"),
            Token.Value("true"),
            Token.Value("true"),
            Token.Equal,
            Token.Value("true"),
            Token.EndOfFile
        )

        val expected = listOf(
            Pattern.Definition(
                "and",
                listOf("true", "true").map { Pattern.Value(it) },
                Pattern.Value("true")
            )
        )
        val got = Parser(source).parse()

        assertEquals(expected, got)
    }

    @Test
    fun `Line Break`() {
        // and :true :true = :true\n
        val source = listOf(
            Token.Identifier("and"),
            Token.Value("true"),
            Token.Value("true"),
            Token.Equal,
            Token.Value("true"),
            Token.LineBreak,
            Token.EndOfFile
        )

        val expected = listOf(
            Pattern.Definition(
                "and",
                listOf("true", "true").map { Pattern.Value(it) },
                Pattern.Value("true")
            )
        )
        val got = Parser(source).parse()
        assertEquals(expected, got)
    }

    @Test
    fun `Line Breaks`() {
        // \n\n\nand :true :true = :true\n\n\n\n
        val source = listOf(
            Token.LineBreak,
            Token.LineBreak,
            Token.Identifier("and"),
            Token.Value("true"),
            Token.Value("true"),
            Token.Equal,
            Token.Value("true"),
            Token.LineBreak,
            Token.LineBreak,
            Token.LineBreak,
            Token.LineBreak,
            Token.EndOfFile
        )

        val expected = listOf(
            Pattern.Definition(
                "and",
                listOf("true", "true").map { Pattern.Value(it) },
                Pattern.Value("true")
            )
        )
        val got = Parser(source).parse()
        assertEquals(expected, got)
    }

    @Test
    fun `Complex Source File`() {
        // and :true :true = :true
        // and _ _ = :false

        // or :false :false = :false
        // or _ _ = :true

        val source = listOf(
            Token.Identifier("and"), Token.Value("true"), Token.Value("true"), Token.Equal, Token.Value("true"),
            Token.LineBreak,
            Token.Identifier("and"), Token.Identifier("_"), Token.Identifier("_"), Token.Equal, Token.Value("false"),
            Token.LineBreak, Token.LineBreak,
            Token.Identifier("or"), Token.Value("false"), Token.Value("false"), Token.Equal, Token.Value("false"),
            Token.LineBreak,
            Token.Identifier("or"), Token.Identifier("_"), Token.Identifier("_"), Token.Equal, Token.Value("true"),
            Token.EndOfFile
        )

        val expected = listOf(
            Pattern.Definition(
                "and",
                listOf("true", "true").map { Pattern.Value(it) },
                Pattern.Value("true")
            ),
            Pattern.Definition(
                "and",
                listOf("_", "_").map { Pattern.Invocation(it, listOf()) },
                Pattern.Value("false")
            ),
            Pattern.Definition(
                "or",
                listOf("false", "false").map { Pattern.Value(it) },
                Pattern.Value("false")
            ),
            Pattern.Definition(
                "or",
                listOf("_", "_").map { Pattern.Invocation(it, listOf()) },
                Pattern.Value("true")
            )
        )
        val got = Parser(source).parse()
        assertEquals(expected, got)
    }

    @Test
    fun `Parenthesised expressions`() {
        // fac :0 = :1
        // fac x = * x (fac (- x :1))

        val source = listOf(
            Token.Identifier("fac"), Token.Value("0"), Token.Equal, Token.Value("1"),
            Token.LineBreak,
            Token.Identifier("fac"), Token.Identifier("x"), Token.Equal,
            Token.Identifier("*"), Token.Identifier("x"),
            Token.LeftParen,
            Token.Identifier("fac"),
            Token.LeftParen,
            Token.Identifier("-"), Token.Identifier("x"), Token.Value("1"),
            Token.RightParen, Token.RightParen, Token.EndOfFile
        )

        val expected = listOf(
            Pattern.Definition(
                "fac",
                listOf("0").map { Pattern.Value(it) },
                Pattern.Value("1")
            ),
            Pattern.Definition(
                "fac",
                listOf("x").map { Pattern.Invocation(it, listOf()) },
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
                                        Pattern.Value("1")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        val got = Parser(source).parse()
        assertEquals(expected, got)
    }

    @Test
    fun `Empty Source`() {
        val expected = listOf<Pattern>()
        val got = Parser(listOf(Token.EndOfFile)).parse()
        assertEquals(expected, got)
    }

    @ExperimentalStdlibApi
    @Test
    fun `Deep parentheses`() {
        // deep = ((((((x y))))))

        val source = buildList {
            add(Token.Identifier("deep"))
            add(Token.Equal)
            repeat(6) {
                add(Token.LeftParen)
            }
            add(Token.Identifier("x"))
            add(Token.Identifier("y"))
            repeat(6) {
                add(Token.RightParen)
            }
            add(Token.EndOfFile)
        }

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
        val got = Parser(source).parse()
        assertEquals(expected, got)
    }

    @ExperimentalStdlibApi
    @Test
    fun `Unmatched parentheses 1`() {
        // deep = (((((x y))))))
        val source = buildList {
            add(Token.Identifier("deep"))
            add(Token.Equal)
            for (v in "(((((") {
                add(Token.LeftParen)
            }
            add(Token.Identifier("x"))
            add(Token.Identifier("y"))
            for (v in "))))))") {
                add(Token.RightParen)
            }
            add(Token.EndOfFile)
        }

        assertThrows(Parser.ParseError::class.java) {
            Parser(source).parse()
        }
    }

    @ExperimentalStdlibApi
    @Test
    fun `Unmatched parentheses 2`() {
        // deep = ((((((x y)))))
        val source = buildList {
            add(Token.Identifier("deep"))
            add(Token.Equal)
            for (v in "((((((") {
                add(Token.LeftParen)
            }
            add(Token.Identifier("x"))
            add(Token.Identifier("y"))
            for (v in ")))))") {
                add(Token.RightParen)
            }
            add(Token.EndOfFile)
        }

        assertThrows(Parser.ParseError::class.java) {
            Parser(source).parse()
        }
    }

    @Test
    fun `Unmatched parentheses 3`() {
        // che = prost)
        val source = listOf(
            Token.Identifier("che"),
            Token.Equal,
            Token.Identifier("prost"),
            Token.RightParen,
            Token.EndOfFile
        )

        assertThrows(Parser.ParseError::class.java) {
            Parser(source).parse()
        }
    }

    @Test
    fun `Unmatched parentheses 4`() {
        // fac :0 = :1
        // fac x = * x (fac (- x :1)
        val source = listOf(
            Token.Identifier("fac"), Token.Value("0"), Token.Equal, Token.Value("1"),
            Token.LineBreak,
            Token.Identifier("fac"), Token.Identifier("x"), Token.Equal,
            Token.Identifier("*"), Token.Identifier("x"), Token.LeftParen,
            Token.Identifier("fac"), Token.LeftParen,
            Token.Identifier("-"), Token.Identifier("x"), Token.Value("1"), Token.RightParen,
            Token.EndOfFile
        )

        assertThrows(Parser.ParseError::class.java) {
            Parser(source).parse()
        }
    }

    @Test
    fun `Unexpected token at start`() {
        // = =
        val suffix = listOf(Token.Equal, Token.Equal, Token.EndOfFile)

        assertAll(
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(listOf(Token.Equal) + suffix).parse()
                }
            },
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(listOf(Token.LeftParen) + suffix).parse()
                }
            },
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(listOf(Token.RightParen) + suffix).parse()
                }
            }
        )
    }

    @Test
    fun `Unexpected invocations`() {
        // def (foo bar) = baz
        val source = listOf(
            Token.Identifier("def"),
            Token.LeftParen, Token.Identifier("foo"), Token.Identifier("bar"), Token.RightParen,
            Token.Equal,
            Token.Identifier("baz"), Token.EndOfFile
        )

        assertThrows(Parser.ParseError::class.java) {
            Parser(source).parse()
        }
    }

    @Test
    fun `Unexpected token at the end of invocation`() {
        // f x y
        val prefix = "f x y".split(" ").map { Token.Identifier(it) }

        assertAll(
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(prefix + listOf(Token.RightParen, Token.EndOfFile)).parse()
                }
            },
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(prefix + listOf(Token.LeftParen, Token.EndOfFile)).parse()
                }
            }
        )
    }

    @Test
    fun `Unexpected token at the end of definition`() {
        // g y x = f x y
        val prefix = listOf(
            Token.Identifier("g"), Token.Identifier("y"), Token.Identifier("x"),
            Token.Equal,
            Token.Identifier("f"), Token.Identifier("x"), Token.Identifier("y")
        )

        assertAll(
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(prefix + listOf(Token.RightParen, Token.EndOfFile)).parse()
                }
            },
            Executable {
                assertThrows(Parser.ParseError::class.java) {
                    Parser(prefix + listOf(Token.LeftParen, Token.EndOfFile)).parse()
                }
            }
        )
    }

    @Test
    fun `Empty definition body`() {
        // empty body =
        val source = listOf(
            Token.Identifier("empty"),
            Token.Identifier("body"),
            Token.Equal,
            Token.EndOfFile
        )

        assertThrows(Parser.ParseError::class.java) {
            Parser(source).parse()
        }
    }

    @Test
    fun `Value expression`() {
        // :true
        val source = listOf(Token.Value("true"), Token.EndOfFile)

        val expected = listOf(
            Pattern.Value("true")
        )
        val got = Parser(source).parse()

        assertEquals(expected, got)
    }
}