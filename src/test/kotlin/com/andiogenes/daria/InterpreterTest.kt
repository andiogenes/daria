package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class InterpreterTest {
    @Test
    fun simple() {
        // and :true :true = :true
        // and _ _ = :false
        //
        // and :true :true
        // and :true :false
        // and :false :true
        // and :false :false
        val and = listOf(
            Pattern.Definition(
                "and",
                listOf(Pattern.Value("true"), Pattern.Value("true")),
                Pattern.Value("true")
            ),
            Pattern.Definition(
                "and",
                listOf(Pattern.Invocation("_", listOf()), Pattern.Invocation("_", listOf())),
                Pattern.Value("false")
            )
        )

        val queries = listOf(
            "true" to "true", "true" to "false",
            "false" to "true", "false" to "false"
        ).map { (x, y) -> Pattern.Invocation("and", listOf(Pattern.Value(x), Pattern.Value(y))) }

        val expected = listOf(
            Pattern.Value("true"),
            Pattern.Value("false"),
            Pattern.Value("false"),
            Pattern.Value("false")
        )

        val got = Interpreter(Scope()).run {
            eval(and)
            queries.map { eval(it) }
        }

        assertEquals(expected, got)
    }

    @Test
    fun `Impossible code 1`() {
        // foo (bar = :baz)
        val impossible = Pattern.Invocation(
            "foo",
            listOf(Pattern.Definition("bar", listOf(), Pattern.Value("baz")))
        )

        assertThrows(Interpreter.RuntimeError::class.java) {
            Interpreter(Scope()).eval(impossible)
        }
    }

    @Test
    fun `Impossible code 2`() {
        // foo :bar = (bar = :baz)
        // foo :bar
        val impossible = listOf(
            Pattern.Definition(
                "foo",
                listOf(Pattern.Value("bar")),
                Pattern.Definition("bar", listOf(), Pattern.Value("baz"))
            ),
            Pattern.Invocation("foo", listOf(Pattern.Value("bar")))
        )

        assertThrows(Interpreter.RuntimeError::class.java) {
            Interpreter(Scope()).eval(impossible)
        }
    }

    @Test
    fun `Value expression`() {
        // :hello
        val expr = Pattern.Value("hello")
        val expected = Pattern.Value("hello")
        val got = Interpreter(Scope()).eval(expr)
        assertEquals(expected, got)
    }
}