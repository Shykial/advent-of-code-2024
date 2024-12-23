package days

import utils.readInputLines

object Day23 {
    fun part1(input: List<String>): Int {
        val computersMap = parseComputersMap(input)

        return computersMap
            .asSequence()
            .filter { it.key.startsWith('t') }
            .flatMap { (computer, others) ->
                others.asSequence().flatMap { other ->
                    (computersMap[other]!! - computer)
                        .asSequence()
                        .filter { t -> t in computersMap[computer]!! }
                        .map { t -> setOf(computer, other, t) }
                }
            }.distinct().count()
    }

    fun part2(input: List<String>): String {
        val computersMap = parseComputersMap(input)
        val remainingToProcess = computersMap.toMutableMap()

        return generateSequence { remainingToProcess.entries.firstOrNull() }
            .map { (computer, others) ->
                val processedComputers = (others + computer).toSet()
                remainingToProcess -= processedComputers

                processedComputers.asSequence()
                    .map { (computersMap[it]!! + it).toSet() }
                    .groupingBy { it intersect processedComputers }
                    .eachCount()
                    .maxBy { it.value }
            }.maxBy { it.value }
            .key.sorted()
            .joinToString(",")
    }

    private fun parseComputersMap(input: List<String>): Map<String, List<String>> =
        input
            .map { it.split('-') }
            .flatMap { (a, b) -> listOf(a to b, b to a) }
            .groupBy({ it.first }, { it.second })
}

fun main() {
    val input = readInputLines("Day23")
    println("part1: ${Day23.part1(input)}")
    println("part2: ${Day23.part2(input)}")
}
