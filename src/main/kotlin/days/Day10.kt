package days

import utils.Coordinates
import utils.Move
import utils.Point
import utils.getOrNull
import utils.plus
import utils.pointsSequence
import utils.readInputLines

object Day10 {
    fun part1(input: List<String>): Int = parseInput(input).countTrailheadsScore()

    fun part2(input: List<String>): Int = parseInput(input).countHikingPathsScore()

    data class TopographicMap(val values: List<List<Int>>) {
        private val starts = values.pointsSequence()
            .filter { it.value == 0 }
            .toList()

        fun countTrailheadsScore() = starts.sumOf { it.countReachableTrailheads(mutableSetOf()) }

        fun countHikingPathsScore() = starts.sumOf { it.countUniqueHikingPaths() }

        private fun Point<Int>.countReachableTrailheads(seenHeads: MutableSet<Coordinates>): Int {
            if (value == 9) return if (seenHeads.add(coordinates)) 1 else 0
            return hikingSequence().sumOf { it.countReachableTrailheads(seenHeads) }
        }

        private fun Point<Int>.countUniqueHikingPaths(): Int {
            if (value == 9) return 1
            return hikingSequence().sumOf { it.countUniqueHikingPaths() }
        }

        private fun Point<Int>.hikingSequence() =
            allowedMoves.asSequence().mapNotNull { move ->
                val newCoords = coordinates + move
                values.getOrNull(newCoords).takeIf { it == value + 1 }?.let { Point(it, newCoords) }
            }
    }

    private val allowedMoves = listOf(
        Move(-1, 0),
        Move(1, 0),
        Move(0, -1),
        Move(0, 1),
    )

    private fun parseInput(input: List<String>) = TopographicMap(input.map { it.map(Char::digitToInt) })
}

fun main() {
    val input = readInputLines("Day10")
    println("part1: ${Day10.part1(input)}")
    println("part2: ${Day10.part2(input)}")
}
