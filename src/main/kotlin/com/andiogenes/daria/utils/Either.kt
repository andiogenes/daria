package com.andiogenes.daria.utils

sealed class Either<out L, out R> {
    data class Left<L>(val l: L) : Either<L, Nothing>()
    data class Right<R>(val r: R) : Either<Nothing, R>()
}