package days

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import utils.pairCombinations
import utils.readInputLines
import utils.splitBy
import java.util.LinkedList
import java.util.PriorityQueue
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.measureTime

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

    fun part2(input: List<String>): String = runBlocking(Dispatchers.Default) {
        val parsed = parseInput(input).let { it.copy(operations = it.operations.sorted()) }
        val randomizedInputs = List(50) { parsed.initialValues.mapValues { Random.nextInt(0..1) } }
        val allPossiblePairs = parsed.operations.pairCombinations().toList()

        val nodesToExplore = PriorityQueue<ExploredNode>(compareBy { it.heuristicCost })
        val startingNode = ExploredNode(pairsSwitched = emptyList(), numberOfIncorrectZs = parsed.incorrectZsOrNull()!!)

        generateSequence(startingNode) { nodesToExplore.poll() }
            .asFlow()
            .onEach { bestNode ->
                if (bestNode.pairsSwitched.size == 4) return@onEach
                nodesToExplore += allPossiblePairs
                    .filter { it.first !in bestNode.affectedPairs && it.second !in bestNode.affectedPairs }
                    .map { pair ->
                        async {
                            parsed.exploredNodeWithPairsOrNull(
                                randomizedInputs = randomizedInputs,
                                newPairs = bestNode.pairsSwitched + pair,
                            )
                        }
                    }.mapNotNull { it.await() }
            }.first { it.heuristicCost == 0 }
            .affectedPairs.map { it.output }
            .sorted()
            .joinToString(",")
    }

    private fun ParsedInput.incorrectZsOrNull(): Int? {
        var x = 0L
        var y = 0L
        for ((key, value) in initialValues) {
            if (value == 0) continue
            when (key.first()) {
                'x' -> x += 1L shl key.drop(1).toInt()
                'y' -> y += 1L shl key.drop(1).toInt()
            }
        }

        var actualZ = 0L
        val values = initialValues.toMutableMap()
        val remainingOperations = operations.toCollection(LinkedList())
        while (remainingOperations.isNotEmpty()) {
            val iterator = remainingOperations.iterator()
            iterator.forEach { element ->
                if (element.first in values && element.second in values) {
                    val result = element.operation(values[element.first]!!, values[element.second]!!)
                    if (result == 1 && element.output.first() == 'z') {
                        actualZ += 1L shl element.output.drop(1).toInt()
                    }
                    values[element.output] = result
                    iterator.remove()
                    continue
                }
            }
            return null
        }

        val bitDiff = (x + y) xor actualZ
        return bitDiff.countOneBits()
    }

    private fun List<Operation>.withSwappedOutputs(
        pairs: Iterable<Pair<Operation, Operation>>,
    ) = toMutableList().apply {
        pairs.forEach { (first, second) ->
            this[indexOf(first)] = first.copy(output = second.output)
            this[indexOf(second)] = second.copy(output = first.output)
        }
    }

    private fun ParsedInput.exploredNodeWithPairsOrNull(
        newPairs: List<Pair<Operation, Operation>>,
        randomizedInputs: List<Map<String, Int>>,
    ): ExploredNode? {
        val newTotalCount = randomizedInputs
            .sumOf {
                ParsedInput(initialValues = it, operations = operations.withSwappedOutputs(newPairs))
                    .incorrectZsOrNull()
                    ?: return null
            }
        return ExploredNode(pairsSwitched = newPairs, numberOfIncorrectZs = newTotalCount)
    }

    private fun List<Operation>.sorted(): List<Operation> {
        val seenOutputs = mutableSetOf<String>()
        val remainingOperations = this.toCollection(LinkedList())
        fun String.isReadyForProcessing() = startsWith('x') || startsWith('y') || this in seenOutputs

        return buildList(this.size) {
            while (remainingOperations.isNotEmpty()) {
                val iterator = remainingOperations.iterator()
                iterator.forEach { element ->
                    if (element.first.isReadyForProcessing() && element.second.isReadyForProcessing()) {
                        this += element
                        seenOutputs += element.output
                        iterator.remove()
                        continue
                    }
                }
            }
        }
    }

    private data class ExploredNode(
        val pairsSwitched: List<Pair<Operation, Operation>>,
        val numberOfIncorrectZs: Int,
    ) {
        val affectedPairs = pairsSwitched.flatMap { it.toList() }.toSet()
        val heuristicCost = (4 - pairsSwitched.size) + numberOfIncorrectZs
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
    measureTime {
        println("part2: ${Day24.part2(input)}")
    }.also { println("Processing took $it") }
}
