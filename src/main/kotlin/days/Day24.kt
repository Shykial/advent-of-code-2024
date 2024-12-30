package days

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import utils.generateFlow
import utils.pairCombinations
import utils.readInputLines
import utils.splitBy
import java.util.LinkedList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.random.Random
import kotlin.random.nextInt

object Day24 {
    private val operationsRegex = Regex("""(\w+) (\w+) (\w+) -> (\w+)""")

    fun part1(input: List<String>): Long = parseInput(input).solveForZOrNull()!!

    private fun Map<String, Int>.randomizedOutputs() = mapValues { Random.nextInt(0..1) }

    fun part2(input: List<String>): String = runBlocking {
        val parsed = parseInput(input).let { it.copy(operations = it.operations.sorted()) }
        val allPossiblePairs = parsed.operations.pairCombinations().toList()

        val searchInputsSampleSizes = sequenceOf(25, 50) + generateSequence(100) { it + 100 }
        val answerCheckInputsSample = List(500) { parsed.initialValues.randomizedOutputs() }
        val initialValue = WiresCombination(
            swappedPairs = emptyList(),
            numberOfIncorrectZs = parsed.incorrectZBitsOrNull()!!,
        )

        searchInputsSampleSizes
            .firstNotNullOf { sampleSize ->
                val randomizedInputs = List(sampleSize) { parsed.initialValues.randomizedOutputs() }
                generateFlow(initialValue) { bestNode ->
                    bestNode.getNextWiresCombination(
                        initialOperations = parsed.operations,
                        allPossiblePairs = allPossiblePairs,
                        randomizedInputs = randomizedInputs,
                    )
                }
                    .take(5)
                    .last()
                    .takeIf { it.isValidAnswer(answerCheckInputsSample, parsed.operations) }
                    .also { if (it == null) println("Processing failed, retrying on increased sample size") }
            }.affectedPairs.map { it.output }
            .sorted()
            .joinToString(",")
    }

    private fun WiresCombination.isValidAnswer(
        answerCheckInputsSample: List<Map<String, Int>>,
        initialOperations: List<Operation>,
    ): Boolean =
        numberOfIncorrectZs == 0 &&
            answerCheckInputsSample.all { sample ->
                ParsedInput(
                    initialValues = sample,
                    operations = initialOperations.withSwappedOutputs(swappedPairs),
                ).incorrectZBitsOrNull() == 0
            }

    private fun ParsedInput.solveForZOrNull(): Long? {
        val values = initialValues.toMutableMap()

        val remainingOperations = operations.toCollection(LinkedList())
        while (remainingOperations.isNotEmpty()) {
            remainingOperations -= remainingOperations
                .firstOrNull { it.first in values && it.second in values }
                ?.also { values[it.output] = it.operation(values[it.first]!!, values[it.second]!!) }
                ?: return null
        }

        return values
            .asSequence()
            .filter { it.key.startsWith('z') && it.value == 1 }
            .map { it.key.drop(1).toInt() }
            .fold(0L) { acc, shift -> acc + (1L shl shift) }
    }

    private suspend fun WiresCombination.getNextWiresCombination(
        allPossiblePairs: List<Pair<Operation, Operation>>,
        randomizedInputs: List<Map<String, Int>>,
        initialOperations: List<Operation>,
    ) = withContext(Dispatchers.Default) {
        logCombinationProcessing(randomizedInputs.size)
        allPossiblePairs
            .filter { it.first !in affectedPairs && it.second !in affectedPairs }
            .map { pair ->
                async {
                    wiresCombinationOrNull(
                        initialOperations = initialOperations,
                        newPairs = swappedPairs + pair,
                        randomizedInputs = randomizedInputs,
                    )
                }
            }.mapNotNull { it.await() }
            .minBy { it.numberOfIncorrectZs }
    }

    private fun WiresCombination.logCombinationProcessing(sampleSize: Int) {
        val effectiveSampleSize = if (swappedPairs.isEmpty()) 1 else sampleSize
        println(
            "Processing wires combination with ${swappedPairs.size} swapped pairs," +
                " sample size: $sampleSize," +
                " average incorrect Zs: ${numberOfIncorrectZs.toDouble() / effectiveSampleSize}",
        )
    }

    private fun ParsedInput.incorrectZBitsOrNull(): Int? {
        var x = 0L
        var y = 0L
        for ((key, value) in initialValues) {
            if (value == 0) continue
            when (key.first()) {
                'x' -> x += 1L shl key.drop(1).toInt()
                'y' -> y += 1L shl key.drop(1).toInt()
            }
        }
        return solveForZOrNull()
            ?.let { x + y xor it }
            ?.countOneBits()
    }

    private fun List<Operation>.withSwappedOutputs(
        pairs: Iterable<Pair<Operation, Operation>>,
    ) = toMutableList().apply {
        pairs.forEach { (first, second) ->
            this[indexOf(first)] = first.copy(output = second.output)
            this[indexOf(second)] = second.copy(output = first.output)
        }
    }

    private fun wiresCombinationOrNull(
        initialOperations: List<Operation>,
        newPairs: List<Pair<Operation, Operation>>,
        randomizedInputs: List<Map<String, Int>>,
    ): WiresCombination? {
        val newOperations = initialOperations.withSwappedOutputs(newPairs)
        val newTotalCount = randomizedInputs.sumOf {
            ParsedInput(initialValues = it, operations = newOperations)
                .incorrectZBitsOrNull()
                ?: return null
        }
        return WiresCombination(swappedPairs = newPairs, numberOfIncorrectZs = newTotalCount)
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

    private data class WiresCombination(
        val swappedPairs: List<Pair<Operation, Operation>>,
        val numberOfIncorrectZs: Int,
    ) {
        val affectedPairs = swappedPairs.flatMap { it.toList() }.toSet()
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
    println("part2: ${Day24.part2(input)}")
}
