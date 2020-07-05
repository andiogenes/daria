package com.andiogenes.daria.expressions

sealed class Pattern {
    data class Definition(val name: String, val args: List<String>, val body: Invocation) : Pattern()
    data class Invocation(val name: String, val args: List<Invocation>) : Pattern()
}