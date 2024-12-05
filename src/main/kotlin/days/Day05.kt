package days

import utils.readInputLines
import utils.splitBy

object Day05 {
    fun part1(input: List<String>): Int = with(parsePages(input)) {
        pageNumbers.asSequence()
            .filter { it.isInCorrectOrder(mustBeBeforeMappings) }
            .sumOf { it[(it.size - 1) / 2] }
    }

    fun part2(input: List<String>): Int = with(parsePages(input)) {
        pageNumbers.asSequence()
            .filterNot { it.isInCorrectOrder(mustBeBeforeMappings) }
            .map { it.withFixedOrdering(mustBeBeforeMappings) }
            .sumOf { it[(it.size - 1) / 2] }
    }

    private fun List<Int>.isInCorrectOrder(mustBeBeforeMappings: Map<Int, Set<Int>>): Boolean {
        val visitedNumbers = mutableSetOf<Int>()
        return all { pageNumber ->
            mustBeBeforeMappings[pageNumber].orEmpty()
                .none { it in visitedNumbers }
                .also { if (it) visitedNumbers += pageNumber }
        }
    }

    private fun List<Int>.withFixedOrdering(mustBeBeforeMappings: Map<Int, Set<Int>>): List<Int> = buildList(size) {
        for (element in this@withFixedOrdering) {
            val followingNumbers = mustBeBeforeMappings[element].orEmpty()
            val beforeIndex = indexOfFirst { it in followingNumbers }
            if (beforeIndex != -1) {
                add(beforeIndex, element)
                continue
            }
            val precedingNumbers = filter { element in mustBeBeforeMappings[it].orEmpty() }
            val afterIndex = indexOfLast { it in precedingNumbers }
            if (afterIndex != -1) {
                add(afterIndex + 1, element)
                continue
            }
            add(element)
        }
    }

    private fun parsePages(pages: List<String>): ParsedPages {
        val (firstPage, secondPage) = pages.splitBy { it.isBlank() }
        val rules = firstPage
            .map { it.split('|') }
            .groupBy(keySelector = { it.first().toInt() }, valueTransform = { it.last().toInt() })
            .mapValues { it.value.toSet() }
        val pageNumbers = secondPage.map { line -> line.split(',').map { it.toInt() } }
        return ParsedPages(rules, pageNumbers)
    }

    private data class ParsedPages(
        val mustBeBeforeMappings: Map<Int, Set<Int>>,
        val pageNumbers: List<List<Int>>,
    )
}

fun main() {
    val input = readInputLines("Day05")
    println("part1: ${Day05.part1(input)}")
    println("part2: ${Day05.part2(input)}")
}
