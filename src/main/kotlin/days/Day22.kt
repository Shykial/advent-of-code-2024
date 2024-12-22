package days

import utils.readInputLines

object Day22 {
    fun part1(input: List<String>): Long =
        input
            .map { it.toLong() }
            .sumOf { initial ->
                generateSequence(initial) { it.nextSecretNumber() }
                    .take(2001)
                    .last()
            }

    fun part2(input: List<String>): Int =
        input
            .map { it.toLong() }
            .flatMap { initialNumber ->
                generateSequence(initialNumber) { it.nextSecretNumber() }
                    .map { it.toString().last().digitToInt() }
                    .take(2001)
                    .windowed(5) { group -> group.zipWithNext { a, b -> b - a } to group.last() }
                    .distinctBy { it.first }
            }.groupingBy { it.first }
            .fold(0) { acc, (_, bananasCount) -> acc + bananasCount }
            .maxOf { it.value }

    private fun Long.nextSecretNumber(): Long {
        val afterMultiplication = (this * 64 mix this).prune()
        val afterDivision = (afterMultiplication / 32 mix afterMultiplication).prune()
        return (afterDivision * 2048 mix afterDivision).prune()
    }

    private infix fun Long.mix(other: Long) = this xor other

    private fun Long.prune() = this % 16777216
}

fun main() {
    val input = readInputLines("Day22")
    println("part1: ${Day22.part1(input)}")
    println("part2: ${Day22.part2(input)}")
}
