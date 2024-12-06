package days

import utils.Coordinates
import utils.Move
import utils.MovingPoint
import utils.Turn
import utils.getOrNull
import utils.plus
import utils.pointsSequence
import utils.readInputLines
import utils.turn

object Day06 {
    private const val START = '^'
    private const val OBSTACLE = '#'

    fun part1(input: List<String>): Int =
        input
            .escapeRoute()
            .distinctBy { it.coordinates }
            .count()

    fun part2(input: List<String>): Int =
        input
            .escapeRoute()
            .zipWithNext()
            .distinctBy { it.second.coordinates }
            .count { input.formsALoop(startingMove = it.first, newObstacle = it.second.coordinates) }

    private fun List<String>.escapeRoute() = generateSequence(getStartingPoint()) { nextPointOrNull(it) }

    private fun List<String>.getStartingPoint() = MovingPoint(
        coordinates = pointsSequence().first { it.value == START }.coordinates,
        move = Move(-1, 0),
    )

    private fun List<String>.nextPointOrNull(movingPoint: MovingPoint): MovingPoint? {
        var currentMove = movingPoint.move
        var nextCoords: Coordinates
        while (true) {
            nextCoords = movingPoint.coordinates + currentMove
            val nextChar = getOrNull(nextCoords)
            when (nextChar) {
                null -> return null // escaped
                OBSTACLE -> currentMove = currentMove.turn(Turn.RIGHT)
                else -> break
            }
        }
        return MovingPoint(nextCoords, currentMove)
    }

    private fun List<String>.formsALoop(
        startingMove: MovingPoint,
        newObstacle: Coordinates,
    ): Boolean {
        val visited = mutableSetOf<MovingPoint>()
        var currentMovingPoint: MovingPoint = startingMove

        while (visited.add(currentMovingPoint)) { // loop detected
            var currentMove = currentMovingPoint.move
            var nextCoords: Coordinates
            while (true) {
                nextCoords = currentMovingPoint.coordinates.plus(currentMove)
                val nextChar = if (nextCoords == newObstacle) OBSTACLE else getOrNull(nextCoords)
                when (nextChar) {
                    null -> return false // got out
                    OBSTACLE -> currentMove = currentMove.turn(Turn.RIGHT)
                    else -> break
                }
            }
            currentMovingPoint = MovingPoint(nextCoords, currentMove)
        }
        return true
    }
}

fun main() {
    val input = readInputLines("Day06")
    println("part1: ${Day06.part1(input)}")
    println("part2: ${Day06.part2(input)}")
}
