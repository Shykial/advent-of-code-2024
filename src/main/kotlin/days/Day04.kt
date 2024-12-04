package days

import utils.Coordinates
import utils.Move
import utils.Point
import utils.getOrNull
import utils.plus
import utils.readInputLines

object Day04 {
    private object XmasWord {
        const val STARTING_CHAR = 'X'
        const val REST_OF_THE_WORD = "MAS"
    }

    private object XmasBox {
        const val MIDDLE_CHAR = 'A'
        val otherChars = setOf('M', 'S')
    }

    fun part1(input: List<String>): Int =
        input.pointsSequence()
            .filter { it.value == XmasWord.STARTING_CHAR }
            .sumOf { countXmasWords(it.coordinates, input) }

    fun part2(input: List<String>): Int =
        input.pointsSequence()
            .filter { it.value == XmasBox.MIDDLE_CHAR }
            .count { formsXmasBox(it.coordinates, input) }

    private fun countXmasWords(startingCoordinates: Coordinates, grid: List<String>): Int =
        moves.count { move ->
            var currentPosition = startingCoordinates
            XmasWord.REST_OF_THE_WORD.all { nextChar ->
                currentPosition += move
                grid.getOrNull(currentPosition) == nextChar
            }
        }

    private fun formsXmasBox(middleCoordinates: Coordinates, grid: List<String>): Boolean =
        diagonalMoves.all { moves ->
            moves
                .mapNotNull { move -> grid.getOrNull(middleCoordinates + move) }
                .toSet() == XmasBox.otherChars
        }

    private fun List<String>.pointsSequence(): Sequence<Point<Char>> =
        asSequence().flatMapIndexed { y, string ->
            string.mapIndexed { x, char -> Point(char, Coordinates(y, x)) }
        }

    private val moves = (-1..1).flatMap { m -> (-1..1).map { Move(m, it) } }

    private val diagonalMoves = listOf(
        listOf(Move(-1, -1), Move(1, 1)),
        listOf(Move(-1, 1), Move(1, -1)),
    )
}

fun main() {
    val input = readInputLines("Day04")
    println("part1: ${Day04.part1(input)}")
    println("part2: ${Day04.part2(input)}")
}
