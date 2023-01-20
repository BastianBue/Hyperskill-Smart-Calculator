package calculator

import java.util.UnknownFormatConversionException
import kotlin.Exception


val IDENTIFIER_REGEX = Regex("[a-zA-Z]+")
val VARIABLE_ASSIGNEMENT_REGEX = Regex("([a-zA-Z]+|\\d+)")

open class AssignmentException(override val message: String?) : Exception(message)
class UnknownVariableException : AssignmentException("Unknown variable")
class InvalidAssignmentException : AssignmentException("Invalid assignment")
class InvalidIdentifierException : AssignmentException("Invalid identifier")

class UserInputException : Exception("Invalid expression")

fun validateLine(line: String) = Regex("[-+]*(\\d+[+-]+)*\\d+").matches(line)

object Calculator {
    private val variables = mutableMapOf<String, Int>()

    fun assign(key: String, value: String) {
        try {
            if (!IDENTIFIER_REGEX.matches(key)) throw InvalidIdentifierException()
            if (!VARIABLE_ASSIGNEMENT_REGEX.matches(value)) throw InvalidAssignmentException()
            if (value.first().isDigit()) {
                variables[key] = value.toInt()
            } else {
                variables[key] = variables[value] ?: throw UnknownVariableException()
            }
        } catch (e: AssignmentException) {
            println(e.message)
        }
    }

    fun lookup(variableName: String): Int? {
        return try {
            variables[variableName] ?: throw UnknownVariableException()
        } catch (e: UnknownFormatConversionException) {
            println(e.message)
            null
        }
    }

    fun evaluateLine(line: String): Int {
        if (!validateLine(line)) throw UserInputException()
        val formattedString = formatLine(line)
        val operations = divideFormattedString(formattedString)
        return evaluateOperations(operations)
    }

    private fun formatLine(operationString: String): String = operationString
        .replace("--", "+")
        .replace("+-", "-")
        .replace("\\++".toRegex(), "+")


    private fun divideFormattedString(formattedLine: String): List<Operation> = buildList {
        val numbers = formattedLine.split("[-+]+".toRegex()).filter { it != "" }.map { it.toInt() }
        val operators = formattedLine.split("\\d+".toRegex()).map {
            when (it) {
                "+" -> Operation.Operators.ADD
                "-" -> Operation.Operators.SUBTRACT
                else -> Operation.Operators.ADD
            }
        }.toMutableList()

        if (numbers.size > operators.size) operators.add(0, Operation.Operators.ADD)
        for (i in numbers.indices) {
            add(
                Operation(
                    operators[i],
                    numbers[i]
                )
            )
        }
    }

    private fun evaluateOperations(operations: List<Operation>): Int {
        var result = 0
        operations.apply { operations.forEach { result = it.applyOperationToValue(result) } }
        return result
    }


    class Operation(
        private val operator: Operators,
        private val number: Int
    ) {
        enum class Operators(val symbol: Char, val transformer: (originalValue: Int, operationValue: Int) -> Int) {
            ADD('+', { originalValue, operationValue -> originalValue + operationValue }),
            SUBTRACT('-', { originalValue, operationValue -> originalValue - operationValue })
        }

        fun applyOperationToValue(originalValue: Int): Int =
            operator.transformer(originalValue, number)
    }
}