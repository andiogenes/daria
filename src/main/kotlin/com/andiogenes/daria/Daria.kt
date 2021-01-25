package com.andiogenes.daria

fun main() {
    val source = """
        and :true :true = :true
        and _ _ = :false

        or :false :false = :false
        or _ _ = :true

        xor :true :true = :false
        xor :false :false = :false
        xor _ _ = :true
        
        and :true :false
    """.trimIndent()
    val lexer = Lexer(source)
    val parser = Parser(lexer.lex())
    val interpreter = Interpreter(Scope())

    val ast = parser.parse()

    interpreter.eval(ast.dropLast(1))
    interpreter.eval(ast.last())?.also { PrettyPrinter().print(listOf(it)) }

    PrettyPrinter().print(ast)
}