package days

import utils.Coordinates
import utils.Move
import utils.Moves
import utils.MovingPoint
import utils.distanceFrom
import utils.getOrNull
import utils.opposite
import utils.plus
import utils.pointsSequence
import utils.readInputLines
import utils.takeWhileInclusive
import kotlin.math.abs

object Day20 {
    private const val START = 'S'
    private const val END = 'E'
    private const val WALL = '#'

    fun part1(input: List<String>): Int = RaceTrack(input).countGoodCheats(2)

    fun part2(input: List<String>): Int = RaceTrack(input).countGoodCheats(20)

    private class RaceTrack(private val values: List<String>) {
        private val start = values.pointsSequence().first { it.value == START }.coordinates
        private val end = values.pointsSequence().first { it.value == END }.coordinates

        fun countGoodCheats(maxCheatLength: Int): Int {
            val scores = distancesFromEnd()
            val winningScore = scores.maxOf { it.value }

            return scores.entries.sumOf { (coords, distanceFromEnd) ->
                val movesSoFar = winningScore - distanceFromEnd
                coords.coordsInRadiusSequence(maxCheatLength)
                    .filter { it.y >= 0 && it.x >= 0 }
                    .count { newCoords ->
                        val otherScore = scores[newCoords]
                        otherScore != null && (movesSoFar + otherScore + coords.distanceFrom(newCoords) <= winningScore - 100)
                    }
            }
        }

        fun distancesFromEnd(): Map<Coordinates, Int> {
            val startingPoint = MovingPoint(end, Move(-10, -10))
            return generateSequence(startingPoint) { point -> point.nextPoint() }
                .takeWhileInclusive { it.coordinates != start }
                .withIndex()
                .associate { (index, value) -> value.coordinates to index }
        }

        private fun MovingPoint.nextPoint() =
            (Moves.Standard - move.opposite()).firstNotNullOf { move ->
                val nextCoords = coordinates + move
                when (values.getOrNull(nextCoords)) {
                    null, WALL -> null
                    else -> MovingPoint(nextCoords, move)
                }
            }

        private fun Coordinates.coordsInRadiusSequence(radius: Int) =
            (-radius..radius).asSequence().flatMap { yShift ->
                val diff = radius - abs(yShift)
                (-diff..diff).asSequence().map { xShift ->
                    Coordinates(y = this.y + yShift, x = this.x + xShift)
                }
            }
    }
}

fun main() {
    val input = readInputLines("Day20")
    println("part1: ${Day20.part1(input)}")
    println("part2: ${Day20.part2(input)}")
}
