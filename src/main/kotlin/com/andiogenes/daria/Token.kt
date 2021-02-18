package com.andiogenes.daria

sealed class Token {
    data class Identifier(val name: String) : Token()
    data class Value(val name: String) : Token()

    object LeftParen : Token()
    object RightParen : Token()

    object Equal : Token()

    object LineBreak : Token()
    object EndOfFile : Token()
}
