package days

import utils.readInputLines

object Day07 {
    fun part1(input: List<String>): Long = input.solve(listOf(Long::plus, Long::times))

    fun part2(input: List<String>): Long = input.solve(listOf(Long::plus, Long::times, { a, b -> "$a$b".toLong() }))

    private fun List<String>.solve(operations: List<(Long, Long) -> Long>) =
        this
            .map { parseLine(it) }
            .filter { it.sumsToResult(it.numbers.first(), 1, operations) }
            .sumOf { it.result }

    private fun RopeEquation.sumsToResult(
        currentValue: Long,
        currentIndex: Int,
        operations: List<(Long, Long) -> Long>,
    ): Boolean = when {
        currentIndex > numbers.lastIndex -> currentValue == result
        else -> operations.any {
            val newAcc = it(currentValue, numbers[currentIndex])
            newAcc <= result && sumsToResult(newAcc, currentIndex + 1, operations)
        }
    }

    private data class RopeEquation(
        val result: Long,
        val numbers: List<Long>,
    )

    private fun parseLine(line: String): RopeEquation {
        val (resultString, numbersString) = line.split(": ")
        val numbers = numbersString.split(' ').map { it.toLong() }
        return RopeEquation(result = resultString.toLong(), numbers = numbers)
    }
}

fun main() {
    val input = readInputLines("Day07")
    println("part1: ${Day07.part1(input)}")
    println("part2: ${Day07.part2(input)}")
}
