package com.andiogenes.daria

import com.andiogenes.daria.expressions.Pattern

typealias Key = String
typealias Value = Pair<List<Pattern>, Pattern>

class Scope {
    private val values = HashMap<Key, ArrayList<Value>>()

    operator fun set(key: Key, value: Value) {
        if (!values.containsKey(key)) {
            values[key] = arrayListOf()
        }

        values[key]?.add(value)
    }

    operator fun get(key: Key, args: List<Pattern.Value>): Value? {
        val definitions = values[key] ?: return null

        var longestMatchIndex = -1
        var longestMatchLength = 0

        for ((i, v) in definitions.withIndex()) {
            val matchLength = args.countMatchesWith(v.first)

            if (args.countMissesWith(v.first) > 0) continue

            if (matchLength > longestMatchLength || longestMatchLength == 0) {
                longestMatchIndex = i
                longestMatchLength = matchLength
            }

            if (matchLength == args.size) break
        }

        if (longestMatchIndex == -1) return null

        return definitions[longestMatchIndex]
    }
}

private fun List<Pattern.Value>.countMatchesWith(other: List<Pattern>): Int =
    this.zip(other).filter { (l, r) -> l == r }.size

private fun List<Pattern.Value>.countMissesWith(other: List<Pattern>): Int =
    this.zip(other).filter { (l, r) -> r is Pattern.Value && l != r }.size