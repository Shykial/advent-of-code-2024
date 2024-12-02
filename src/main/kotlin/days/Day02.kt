package days

import utils.Regexes
import utils.minusIndex
import utils.readInputLines

object Day02 {
    fun part1(input: List<String>): Int = input.asSequence()
        .map { parseLine(it) }
        .count { l -> isSafe(l) }

    fun part2(input: List<String>): Int = input.asSequence()
        .map { parseLine(it) }
        .count { line ->
            line.indices
                .asSequence()
                .map { line.minusIndex(it) }
                .any { isSafe(it) }
        }

    private fun isSafe(report: List<Int>): Boolean {
        val multiplier = if (report[0] > report[1]) 1 else -1
        return report
            .asSequence()
            .zipWithNext { a, b -> (a - b) * multiplier }
            .all { it in 1..3 }
    }

    private fun parseLine(line: String) = line.split(Regexes.WhiteSpace).map { it.toInt() }
}

fun main() {
    val input = readInputLines("Day02")
    println("part1: ${Day02.part1(input)}")
    println("part2: ${Day02.part2(input)}")
}
