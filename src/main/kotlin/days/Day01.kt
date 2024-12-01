package days

import utils.Regexes
import utils.groupByCount
import utils.readInputLines
import utils.transpose
import kotlin.math.abs

object Day01 {
    fun part1(input: List<String>): Int {
        val (sortedFirstIds, sortedSecondIds) = parseLines(input).map { it.sorted() }
        return sortedFirstIds.zip(sortedSecondIds) { a, b -> abs(a - b) }.sum()
    }

    fun part2(input: List<String>): Int {
        val (firstIds, secondIds) = parseLines(input)
        val secondIdsByCount = secondIds.groupByCount()
        return firstIds.sumOf { it * (secondIdsByCount[it] ?: 0) }
    }

    private fun parseLines(lines: List<String>) = lines
        .map { line -> line.split(Regexes.WhiteSpace).map { it.toInt() } }
        .transpose()
}

fun main() {
    val input = readInputLines("Day01")
    println("part1: ${Day01.part1(input)}")
    println("part2: ${Day01.part2(input)}")
}
