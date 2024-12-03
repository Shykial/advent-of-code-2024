package days

import utils.readInputLines

object Day03 {
    private val multipliersRegex = Regex("""mul\((\d+),(\d+)\)""")
    private val conditionalSectorRegex = Regex("""(?:^|do\(\))(.*?)(?:don't\(\)|$)""")

    fun part1(input: List<String>): Int = input.joinToString("").sumMultiplications()

    fun part2(input: List<String>): Int =
        input.joinToString("")
            .let { conditionalSectorRegex.findAll(it) }
            .sumOf { it.groupValues.last().sumMultiplications() }

    private fun String.sumMultiplications() =
        multipliersRegex.findAll(this).sumOf {
            val (first, second) = it.destructured
            first.toInt() * second.toInt()
        }
}

fun main() {
    val input = readInputLines("Day03")
    println("part1: ${Day03.part1(input)}")
    println("part2: ${Day03.part2(input)}")
}
