package com.andiogenes.daria

fun main() {
    /**
     * Проблемес:
     * 1. Не совсем правильная модель паттерн-матчинга
     * 2. К локальной области видимости не присоединяются отброшенные при zip значения
     * 3. Локальная область видимости привязана к ближайшим аргументам шаблона
     */

    val source = """
        and true true = true
        and _ _ = false

        or false false = false
        or _ _ = true

        xor true true = false
        xor false false = false
        xor _ _ = true
        
        and true false
    """.trimIndent()
    val lexer = Lexer(source)
    val parser = Parser(lexer.lex())
    val interpreter = Interpreter(Scope())

    val ast = parser.parse()

    interpreter.eval(ast.dropLast(1))
    println(interpreter.eval(ast.last()))

    PrettyPrinter().eval(ast)
}