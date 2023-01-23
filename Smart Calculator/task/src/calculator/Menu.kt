package calculator

val COMMAND_REGEX = Regex("/.*")
val ASSIGNMENT_REGEX = Regex(".*=.*")
val VARIABLE_LOOKUP_REGEX = Regex("[a-zA-Z]+")

fun menu() {
    do {
        val line = readln().replace("\\s".toRegex(), "")
        try {
            when {
                line == "" -> continue

                COMMAND_REGEX.matches(line) -> when (line) {
                    "/exit" -> continue
                    "/help" -> println("help me!")
                    else -> println("Unknown command")
                }

                ASSIGNMENT_REGEX.matches(line) -> {
                    val (key, value) = line.split("=")
                    Calculator.assign(key, value)
                }

                VARIABLE_LOOKUP_REGEX.matches(line) -> println(Calculator.lookup(line))

                else -> println(Calculator.evaluateLine(line))
            }

        } catch (e: UserInputException) {
            println(e.message)
        }
    } while (line != "/exit")
    println("Bye!")
}
