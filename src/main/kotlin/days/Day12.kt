package days

import utils.Moves
import utils.MovingPoint
import utils.Point
import utils.Turn
import utils.getOrNull
import utils.plus
import utils.pointsSequence
import utils.turn

object Day12 {
    fun part1(input: List<String>): Int {
        val points = input.pointsSequence().associateBy { it.coordinates }.toMap(LinkedHashMap())

        data class Chunk(val area: Int, val perimeter: Int)

        fun exploreChunk(point: Point<Char>): Chunk {
            var currentPerimeter = 0
            val area = generateSequence(listOf(point)) { currentPoints ->
                currentPoints.flatMap { point ->
                    Moves.Standard.mapNotNull { move ->
                        val newCoords = point.coordinates + move
                        input.getOrNull(newCoords)
                            ?.takeIf { it == point.value }
                            ?.let { Point(value = it, coordinates = newCoords) }
                    }
                        .also { currentPerimeter += 4 - it.size }
                        .filter { it.coordinates in points }
                        .onEach { points.remove(it.coordinates) }
                }.takeIf { it.isNotEmpty() }
            }.sumOf { it.size }
            return Chunk(area = area, perimeter = currentPerimeter)
        }
        return generateSequence { points.pollFirstEntry() }
            .map { exploreChunk(it.value) }
            .sumOf { it.area * it.perimeter }
    }


    fun part2(input: List<String>): Int {
        val points = input.pointsSequence().associateBy { it.coordinates }.toMap(LinkedHashMap())

        data class ChunkTwo(val area: Int, val numberOfSides: Int)

        fun exploreChunk(point: Point<Char>): ChunkTwo {
            val visitedEdges = LinkedHashSet<Point<Char>>()
            var currentPerimeter = 0
            val area = generateSequence(listOf(point)) { currentPoints ->
                currentPoints.flatMap { point ->
                    Moves.Standard.mapNotNull { move ->
                        val newCoords = point.coordinates + move
                        input.getOrNull(newCoords)
                            ?.takeIf { it == point.value }
                            ?.let { Point(value = it, coordinates = newCoords) }
                    }
                        .also {
                            if (it.size < 4) visitedEdges += point
                            currentPerimeter += 4 - it.size
                        }
                        .filter { it.coordinates in points }
                        .onEach { points.remove(it.coordinates) }
                }.takeIf { it.isNotEmpty() }
            }.sumOf { it.size }

            var sides = 0
            val initialPoint = visitedEdges.first
            val edgeTurn = Turn.RIGHT

            val startingPoint = Moves.Standard
                .firstNotNullOf { move ->
                    val nextCoords = initialPoint.coordinates + move
                    nextCoords
                        .takeIf { input.getOrNull(it) != initialPoint.value }
                        ?.let { MovingPoint(nextCoords, move.turn(edgeTurn)) }
                }
            var currentPoint = startingPoint
            do {
                val simplePoint = listOf(currentPoint.move, currentPoint.move.turn(Turn.LEFT)).asSequence()
                    .map { move ->
                        when (move) {
                            currentPoint.move -> MovingPoint(currentPoint.coordinates + move, move)
                            else -> MovingPoint(currentPoint.coordinates, move)
                        }
                    }.firstOrNull {
                        input.getOrNull(it.coordinates) != initialPoint.value
                            && input.getOrNull(it.coordinates + it.move.turn(edgeTurn)) == initialPoint.value
                    }

                val newPoint = simplePoint ?: run {
                    val newMove = currentPoint.move.turn(edgeTurn)
                    val newCoords = currentPoint.coordinates + currentPoint.move + newMove
                    MovingPoint(newCoords, newMove)
                }

                if (newPoint.move != currentPoint.move) sides++
                currentPoint = newPoint
            } while (currentPoint != startingPoint)

            return ChunkTwo(area = area, numberOfSides = sides)
        }

        return generateSequence { points.pollFirstEntry() }
            .map { exploreChunk(it.value) }
            .sumOf { it.area * it.numberOfSides }
    }
}


fun main() {
//    val input = readInputLines("Day12")

    val input = """
                XXXXX
                XX.XX
                XX.XX
                XX.XX
                XX.XX
    """.trimIndent().lines()


    println("part1: ${Day12.part1(input)}")
//    measureTime {
//        repeat(100) {
//            Day12.part1(input)
//        }
//    }.also { println(it / 100) }
    println("part2: ${Day12.part2(input)}")
}