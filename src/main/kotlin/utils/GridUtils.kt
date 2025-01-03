package utils

import kotlin.math.abs

data class Coordinates(val y: Int, val x: Int)
data class LongCoordinates(val y: Long, val x: Long)

data class Move(val yShift: Int, val xShift: Int) {
    companion object {
        val Up = Move(-1, 0)
        val Down = Move(1, 0)
        val Right = Move(0, 1)
        val Left = Move(0, -1)
    }
}

object Moves {
    val Standard = listOf(Move.Up, Move.Down, Move.Left, Move.Right)
}

operator fun Coordinates.plus(move: Move) = Coordinates(y = y + move.yShift, x = x + move.xShift)
operator fun LongCoordinates.plus(move: Move) = LongCoordinates(y = y + move.yShift, x = x + move.xShift)

operator fun Coordinates.minus(move: Move) = Coordinates(y = y - move.yShift, x = x - move.xShift)

operator fun Coordinates.minus(other: Coordinates) = Move(yShift = y - other.y, xShift = x - other.x)

fun List<CharSequence>.getOrNull(coordinates: Coordinates): Char? = getOrNull(coordinates.y)?.getOrNull(coordinates.x)

@JvmName("getOrNullCharArray")
fun List<CharArray>.getOrNull(coordinates: Coordinates): Char? = getOrNull(coordinates.y)?.getOrNull(coordinates.x)

fun <T> List<List<T>>.getOrNull(coordinates: Coordinates): T? = getOrNull(coordinates.y)?.getOrNull(coordinates.x)

operator fun List<CharSequence>.contains(coordinates: Coordinates) = getOrNull(coordinates) != null

operator fun List<CharArray>.get(coordinates: Coordinates) = this[coordinates.y][coordinates.x]

operator fun List<CharArray>.set(coordinates: Coordinates, value: Char) {
    this[coordinates.y][coordinates.x] = value
}

data class Point<T>(val value: T, val coordinates: Coordinates)

enum class Turn { LEFT, RIGHT }

fun Turn.opposite() = when (this) {
    Turn.LEFT -> Turn.RIGHT
    Turn.RIGHT -> Turn.LEFT
}

infix fun Move.turn(turn: Turn) = when (turn) {
    Turn.LEFT -> Move(yShift = -xShift, xShift = yShift)
    Turn.RIGHT -> Move(yShift = xShift, xShift = -yShift)
}

fun Move.opposite() = Move(yShift = -yShift, xShift = -xShift)

data class MovingPoint(val coordinates: Coordinates, val move: Move)

@JvmName("stringsPointsSequence")
fun List<String>.pointsSequence(): Sequence<Point<Char>> =
    asSequence().flatMapIndexed { y, string ->
        string.mapIndexed { x, char -> Point(char, Coordinates(y, x)) }
    }

fun <T> List<List<T>>.pointsSequence(): Sequence<Point<T>> =
    asSequence().flatMapIndexed { y, row ->
        row.mapIndexed { x, element -> Point(element, Coordinates(y, x)) }
    }

fun Coordinates.distanceFrom(other: Coordinates) = abs(y - other.y) + abs(x - other.x)
