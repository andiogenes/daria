package com.andiogenes.daria

fun main() {
    val stdlib = """
        and :true :true = :true
        and _ _ = :false

        or :false :false = :false
        or _ _ = :true

        xor :true :true = :false
        xor :false :false = :false
        xor _ _ = :true
    """.trimIndent()

    val lexer = Lexer(stdlib)
    val parser = Parser(lexer.lex())
    val interpreter = Interpreter(Scope())

    val ast = parser.parse()

    interpreter.eval(ast)

    // REPL
    while (true) {
        print("> ")
        val line = readLine()!!
        if (line.startsWith(";exit")) break

        Lexer(line).lex().let {
            Parser(it).parse()
        }.let {
            interpreter.eval(it.first())
        }?.let {
            println(":${it.name}")
        }
    }
}