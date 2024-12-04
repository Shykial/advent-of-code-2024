package utils

data class Coordinates(val y: Int, val x: Int)

data class Move(val yShift: Int, val xShift: Int)

operator fun Coordinates.plus(move: Move) = Coordinates(y + move.yShift, x + move.xShift)

fun List<CharSequence>.getOrNull(coordinates: Coordinates): Char? = getOrNull(coordinates.y)?.getOrNull(coordinates.x)

data class Point<T>(val value: T, val coordinates: Coordinates)
