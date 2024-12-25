package days

import utils.readInputLines
import utils.splitBy
import java.util.LinkedList

object Day24 {
    private val operationsRegex = Regex("""(\w+) (\w+) (\w+) -> (\w+)""")

    fun part1(input: List<String>): Long {
        val parsedInput = parseInput(input)
        val values = parsedInput.initialValues.toMutableMap()

        val remainingOperations = parsedInput.operations.toCollection(LinkedList())
        while (remainingOperations.isNotEmpty()) {
            remainingOperations -= remainingOperations
                .first { it.first in values && it.second in values }
                .also { values[it.output] = it.operation(values[it.first]!!, values[it.second]!!) }
        }

        return values
            .asSequence()
            .filter { it.key.startsWith('z') && it.value == 1 }
            .map { it.key.drop(1).toInt() }
            .fold(0L) { acc, shift -> acc + (1L shl shift) }
    }

    private data class ParsedInput(
        val initialValues: Map<String, Int>,
        val operations: List<Operation>,
    )

    private data class Operation(
        val first: String,
        val second: String,
        val output: String,
        val operation: (Int, Int) -> Int,
    )

    private fun parseInput(input: List<String>): ParsedInput {
        val (valuesStrings, operationsStrings) = input.splitBy { it.isBlank() }
        val values = valuesStrings.associate {
            it.split(": ").let { (name, value) -> name to value.toInt() }
        }
        val operations = operationsStrings.map { parseOperationString(it) }
        return ParsedInput(values, operations)
    }

    private fun parseOperationString(it: String): Operation {
        val (first, operation, second, output) = operationsRegex.find(it)!!.destructured
        return Operation(first, second, output, parseOperation(operation))
    }

    private fun parseOperation(operationString: String) = when (operationString) {
        "AND" -> Int::and
        "OR" -> Int::or
        "XOR" -> Int::xor
        else -> error("Invalid operation string: $operationString")
    }
}

fun main() {
    val input = readInputLines("Day24")
    println("part1: ${Day24.part1(input)}")
}
