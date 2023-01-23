package calculator

import java.math.BigInteger
import java.util.Stack
import kotlin.Exception


val IDENTIFIER_REGEX = Regex("[a-zA-Z]+")
val VARIABLE_ASSIGNEMENT_REGEX = Regex("-?([a-zA-Z]+|\\d+)")
val OPERATOR_REGEX = Regex("[+-/*()]")

open class VariableException(override val message: String?) : Exception(message)
class UnknownVariableException : VariableException("Unknown variable")
class InvalidAssignmentException : VariableException("Invalid assignment")
class InvalidIdentifierException : VariableException("Invalid identifier")
class UserInputException : Exception("Invalid expression")

fun validateInfixNotation(notation: String) =
    Regex("[-+*/]*(\\(*(\\d+\\)*|\\(?[a-zA-Z]+\\)*)[-+*/]+)*(\\d+|[a-zA-Z]+\\)*)\\)*").matches(notation)

fun isOperator(it: Char) = OPERATOR_REGEX.matches(it.toString())

object Calculator {
    private val variables = mutableMapOf<String, BigInteger>()

    fun assign(key: String, value: String) {
        try {
            if (!IDENTIFIER_REGEX.matches(key)) throw InvalidIdentifierException()
            if (!VARIABLE_ASSIGNEMENT_REGEX.matches(value)) throw InvalidAssignmentException()
            if (value.toBigIntegerOrNull() != null) {
                variables[key] = value.toBigInteger()
            } else {
                variables[key] = variables[value] ?: throw UnknownVariableException()
            }
        } catch (e: VariableException) {
            println(e.message)
        }
    }

    fun lookup(variableName: String): BigInteger? {
        return try {
            variables[variableName] ?: throw UnknownVariableException()
        } catch (e: UnknownVariableException) {
            println(e.message)
            null
        }
    }

    private fun formatLine(operationString: String): String {
        if (operationString.matches(".*(\\*\\*|//).*".toRegex())) throw UserInputException()
        return operationString
            .replace("--", "+")
            .replace("+-", "-")
            .replace("\\++".toRegex(), "+")
            .replace("\\*+".toRegex(), "*")
            .replace("/+".toRegex(), "/")

    }

    fun convertToPostFix(line: String): String {
        if (!validateInfixNotation(line)) throw UserInputException()

        val operatorStack = Stack<Char>()
        var postFixNotation = ""

        val getPrecidence = { operator: Char ->
            when (operator) {
                '+', '-' -> 1
                '*', '/' -> 2
                else -> throw Exception("encountered an invalid operator $operator while converting expression to postfix")
            }
        }
        var digits = ""
        line.forEach {
            if (isOperator(it) && digits.isNotEmpty()) {
                postFixNotation += "$digits "
                digits = ""
            }
            // add teh spaces
            if (!isOperator(it)) digits += it
            else if (it == '(') operatorStack.push(it)
            else if (it == ')') {
                do {
                    postFixNotation += "${operatorStack.pop()} "
                    if (operatorStack.isEmpty()) throw UserInputException()
                } while (operatorStack.peek() != '(')
                operatorStack.pop()
            } else if (operatorStack.isEmpty() || operatorStack.peek() == '(') operatorStack.push(it)
            else if (getPrecidence(it) > getPrecidence(operatorStack.peek())) operatorStack.push(it)
            else if (getPrecidence(it) <= getPrecidence(operatorStack.peek())) {
                val precidence = getPrecidence(it)
                do {
                    postFixNotation += "${operatorStack.pop()} "
                } while (!operatorStack.isEmpty() && operatorStack.peek() != '(' && getPrecidence(operatorStack.peek()) > precidence)
                operatorStack.push(it)
            }
        }
        if (operatorStack.contains('(')) throw UserInputException()

        if (digits.isNotEmpty()) postFixNotation += "$digits "
        repeat(operatorStack.size) {
            postFixNotation += "${operatorStack.pop()} "
        }
        return postFixNotation.trim()
    }

    private fun evaluatePostFix(postfix: String): BigInteger {
        val stack = Stack<BigInteger>()
        val items = postfix.split(" ")
        for (i in items.indices) {
            val operator = items[i]
            var number: BigInteger? = null
            if (!OPERATOR_REGEX.matches(operator)) number = operator.toBigIntegerOrNull() ?: lookup(operator)
            if (number != null) {
                stack.push(number)
            } else {
                val (x, y) = List(2) { stack.pop() }
                when (operator) {
                    "+" -> stack.push(y + x)
                    "-" -> stack.push(y - x)
                    "*" -> stack.push(y * x)
                    "/" -> stack.push(y / x)
                }
            }
        }
        return stack.pop()
    }

    fun evaluateLine(line: String): BigInteger {
        val formattedLine = formatLine(line)
        val postFix = convertToPostFix(formattedLine)
        return evaluatePostFix(postFix)
    }

}








