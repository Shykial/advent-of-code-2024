package days

import utils.readInputLines
import utils.splitBy

object Day19 {
    fun part1(input: List<String>): Int = with(parseInput(input)) {
        patterns.count { isPossibleDesign(it, availableTowels) }
    }

    fun part2(input: List<String>): Long = with(parseInput(input)) {
        patterns.sumOf { countAvailableDesigns(it, availableTowels) }
    }

    private val possibleDesignsCache = mutableMapOf<String, Boolean>()

    private fun isPossibleDesign(
        remainingPattern: String,
        towels: List<String>,
    ): Boolean = possibleDesignsCache.getOrPut(remainingPattern) {
        remainingPattern.isEmpty() ||
            towels.asSequence()
                .filter { remainingPattern.startsWith(it) }
                .any { isPossibleDesign(remainingPattern.substringAfter(it), towels) }
    }

    private val numberOfPossibilitiesPerDesignCache = mutableMapOf<String, Long>()

    private fun countAvailableDesigns(
        remainingPattern: String,
        towels: List<String>,
    ): Long = numberOfPossibilitiesPerDesignCache.getOrPut(remainingPattern) {
        if (remainingPattern.isEmpty()) 1L
        else towels.asSequence()
            .filter { remainingPattern.startsWith(it) }
            .sumOf { countAvailableDesigns(remainingPattern.substringAfter(it), towels) }
    }

    private data class Towels(
        val availableTowels: List<String>,
        val patterns: List<String>,
    )

    private fun parseInput(input: List<String>): Towels {
        val (availableString, patternsStrings) = input.splitBy { it.isBlank() }
        return Towels(
            availableTowels = availableString.single().split(", "),
            patterns = patternsStrings,
        )
    }
}

fun main() {
    val input = readInputLines("Day19")
    println("part1: ${Day19.part1(input)}")
    println("part2: ${Day19.part2(input)}")
}
