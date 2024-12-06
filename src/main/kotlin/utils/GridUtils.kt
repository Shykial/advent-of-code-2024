package utils

data class Coordinates(val y: Int, val x: Int)

data class Move(val yShift: Int, val xShift: Int)

operator fun Coordinates.plus(move: Move) = Coordinates(y + move.yShift, x + move.xShift)

fun List<CharSequence>.getOrNull(coordinates: Coordinates): Char? = getOrNull(coordinates.y)?.getOrNull(coordinates.x)

data class Point<T>(val value: T, val coordinates: Coordinates)

enum class Turn { LEFT, RIGHT }

infix fun Move.turn(turn: Turn) = when (turn) {
    Turn.LEFT -> Move(yShift = -xShift, xShift = yShift)
    Turn.RIGHT -> Move(yShift = xShift, xShift = -yShift)
}

data class MovingPoint(val coordinates: Coordinates, val move: Move)

fun List<String>.pointsSequence(): Sequence<Point<Char>> =
    asSequence().flatMapIndexed { y, string ->
        string.mapIndexed { x, char -> Point(char, Coordinates(y, x)) }
    }
