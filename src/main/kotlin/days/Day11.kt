package days

import utils.cutInHalf
import utils.groupByCount
import utils.map
import utils.readInput

object Day11 {
    fun part1(input: String): Long {
        val stones = parseInput(input)
        return generateSequence(stones) { it.flatMap { s -> blinkSingleStone(s) } }
            .take(26)
            .last()
            .size.toLong()
    }

    fun part2(input: String): Long {
        val countedStones = parseInput(input).groupByCount().mapValues { it.value.toLong() }
        return generateSequence(countedStones) { oldCounts -> blinkCountedStones(oldCounts) }
            .take(76)
            .last()
            .values.sum()
    }

    private fun blinkSingleStone(oldStone: Long): List<Long> = when {
        oldStone == 0L -> listOf(1)
        oldStone.toString().length % 2 == 0 -> oldStone.toString().cutInHalf().map { it.toLong() }.toList()
        else -> listOf(oldStone * 2024)
    }

    private fun blinkCountedStones(countedStones: Map<Long, Long>): Map<Long, Long> = buildMap {
        countedStones.forEach { (number, count) ->
            blinkSingleStone(number).forEach {
                merge(it, count) { oldCount, _ -> oldCount + count }
            }
        }
    }

    private fun parseInput(input: String) = input.split(' ').map { it.toLong() }
}

fun main() {
    val input = readInput("Day11")
    println("part1: ${Day11.part1(input)}")
    println("part2: ${Day11.part2(input)}")
}
