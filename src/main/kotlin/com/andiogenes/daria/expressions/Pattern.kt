package com.andiogenes.daria.expressions

sealed class Pattern {
    abstract val fullName: String

    data class Definition(val name: String, val args: List<Pattern>, val body: Pattern) : Pattern() {
        override val fullName: String get() = name
    }
    data class Invocation(val name: String, val args: List<Pattern>) : Pattern() {
        override val fullName: String get() = name
    }
    data class Value(val name: String) : Pattern() {
        override val fullName: String get() = name
    }
}
