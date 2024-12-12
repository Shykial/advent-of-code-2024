package days

import utils.Coordinates
import utils.Moves
import utils.MovingPoint
import utils.Point
import utils.Turn
import utils.getOrNull
import utils.opposite
import utils.plus
import utils.pointsSequence
import utils.readInputLines
import utils.turn

object Day12 {

    private fun List<String>.getRegionSiblings(point: Point<Char>) = Moves.Standard.mapNotNull { move ->
        val newCoords = point.coordinates + move
        getOrNull(newCoords)
            ?.takeIf { it == point.value }
            ?.let { Point(value = it, coordinates = newCoords) }
    }

    fun part1(input: List<String>): Int {
        val points = input.pointsSequence().associateBy { it.coordinates }.toMap(LinkedHashMap())

        fun countPriceOfRegion(point: Point<Char>): Int {
            var currentPerimeter = 0
            val area = generateSequence(listOf(point)) { currentPoints ->
                currentPoints.flatMap { point ->
                    input.getRegionSiblings(point)
                        .also { currentPerimeter += 4 - it.size }
                        .filter { points.remove(it.coordinates) != null }
                }.takeIf { it.isNotEmpty() }
            }.sumOf { it.size }
            return area * currentPerimeter
        }
        return generateSequence { points.pollFirstEntry() }.sumOf { countPriceOfRegion(it.value) }
    }

    fun part2(input: List<String>): Int {
        val points = input.pointsSequence().associateBy { it.coordinates }.toMap(LinkedHashMap())

        fun countPriceOfRegion(point: Point<Char>): Int {
            val visitedEdges = LinkedHashSet<Coordinates>()
            val area = generateSequence(listOf(point)) { currentPoints ->
                currentPoints.flatMap { point ->
                    input.getRegionSiblings(point)
                        .also { if (it.size < 4) visitedEdges += point.coordinates }
                        .filter { points.remove(it.coordinates) != null }
                }.takeIf { it.isNotEmpty() }
            }.sumOf { it.size }

            var numberOfSides = 0
            val visitedPoints = mutableSetOf<MovingPoint>()
            val turn = Turn.RIGHT
            visitedEdges.asSequence()
                .map { initialPoint ->
                    Moves.Standard.firstNotNullOf { move ->
                        val nextCoords = (initialPoint + move)
                        nextCoords.takeIf { input.getOrNull(it) != point.value }
                            ?.let { MovingPoint(nextCoords, move.turn(turn)) }
                    }
                }.filter { visitedPoints.add(it) }
                .forEach { startingPoint ->
                    var currentPoint = startingPoint
                    do {
                        val simplePoint =
                            listOf(currentPoint.move, currentPoint.move.turn(turn.opposite())).asSequence()
                                .map { move ->
                                    when (move) {
                                        currentPoint.move -> MovingPoint(currentPoint.coordinates + move, move)
                                        else -> MovingPoint(currentPoint.coordinates, move)
                                    }
                                }.firstOrNull {
                                    input.getOrNull(it.coordinates) != point.value &&
                                        it.coordinates + it.move.turn(turn) in visitedEdges &&
                                        input.getOrNull(it.coordinates + it.move.turn(turn)) == point.value
                                }

                        val newPoint = simplePoint ?: run {
                            val newMove = currentPoint.move.turn(turn)
                            val newCoords = currentPoint.coordinates + currentPoint.move + newMove
                            MovingPoint(newCoords, newMove)
                        }

                        if (newPoint.move != currentPoint.move) numberOfSides++
                        currentPoint = newPoint
                        visitedPoints += currentPoint
                    } while (currentPoint != startingPoint)
                }

            return area * numberOfSides
        }

        return generateSequence { points.pollFirstEntry() }.sumOf { countPriceOfRegion(it.value) }
    }
}

fun main() {
    val input = readInputLines("Day12")
    println("part1: ${Day12.part1(input)}")
    println("part2: ${Day12.part2(input)}")
}
