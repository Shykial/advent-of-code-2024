package days

import utils.Move
import utils.Point
import utils.get
import utils.plus
import utils.pointsSequence
import utils.readInputLines
import utils.set
import utils.splitBy
import utils.takeWhileInclusive

object Day15 {
    private const val ROBOT = '@'
    private const val BOX = 'O'
    private const val LARGE_BOX = "[]"
    private const val WALL = '#'
    private const val FREE_SPACE = '.'

    fun part1(input: List<String>): Int {
        val (grid, moves) = parseInput(input)
        return StandardWarehouseMap(grid)
            .apply { moves.forEach { makeMove(it) } }
            .sumGpsValues()
    }

    fun part2(input: List<String>): Int {
        val (grid, moves) = parseInput(input)
        return ScaledUpWarehouseMap(grid.map { it.adjustedForPart2() })
            .apply { moves.forEach { makeMove(it) } }
            .sumGpsValues()
    }

    private fun parseInput(input: List<String>): ParsedInput {
        val (grid, movesStrings) = input.splitBy { it.isBlank() }
        return ParsedInput(
            grid = grid,
            robotMoves = movesStrings.flatMap { line -> line.map { Move.fromChar(it) } },
        )
    }

    private abstract class WarehouseMap(initialValues: List<String>) {
        protected var currentPosition = initialValues.pointsSequence().first { it.value == ROBOT }
        protected val values = initialValues.map { it.toCharArray() }
        protected abstract val gpsChar: Char

        abstract fun makeMove(move: Move)

        protected fun setNewCurrent(point: Point<Char>) {
            values[currentPosition.coordinates] = FREE_SPACE
            values[point.coordinates] = ROBOT
            currentPosition = Point(ROBOT, point.coordinates)
        }

        protected fun Point<Char>.move(move: Move): Point<Char> {
            val nextCoords = coordinates + move
            return Point(values[nextCoords], nextCoords)
        }

        fun sumGpsValues(): Int = values.withIndex().sumOf { (rowIndex, row) ->
            row.withIndex().asSequence()
                .filter { (_, char) -> char == gpsChar }
                .sumOf { (columnIndex, _) -> rowIndex * 100 + columnIndex }
        }
    }

    private class StandardWarehouseMap(initialValues: List<String>) : WarehouseMap(initialValues) {
        override val gpsChar = BOX

        override fun makeMove(move: Move) {
            val nextPoint = currentPosition.move(move)
            when (nextPoint.value) {
                WALL -> return
                FREE_SPACE -> {
                    setNewCurrent(nextPoint)
                    return
                }
            }
            val boxesToMove = generateSequence(nextPoint) { it.move(move) }
                .takeWhileInclusive { it.value == 'O' }
                .toList()
                .also { if (it.last().value == WALL) return }

            setNewCurrent(boxesToMove.first())
            values[boxesToMove.last().coordinates] = BOX
        }
    }

    private class ScaledUpWarehouseMap(initialValues: List<String>) : WarehouseMap(initialValues) {
        override val gpsChar = LARGE_BOX.first()

        override fun makeMove(move: Move) {
            if (move.xShift == 0) moveVertically(move) else moveHorizontally(move)
        }

        private fun moveHorizontally(move: Move) {
            val nextPoint = currentPosition.move(move)
            when (nextPoint.value) {
                WALL -> return
                FREE_SPACE -> {
                    setNewCurrent(nextPoint)
                    return
                }
            }
            val boxesToMove = generateSequence(nextPoint) { it.move(move) }
                .takeWhileInclusive { it.value in LARGE_BOX }
                .toList()
                .also { if (it.last().value == WALL) return }

            setNewCurrent(boxesToMove.first())
            val indexOffset = if (move.xShift == 1) 0 else 1
            boxesToMove.asSequence()
                .drop(1)
                .forEachIndexed { index, point ->
                    values[point.coordinates] = LARGE_BOX[(index + indexOffset) % LARGE_BOX.length]
                }
        }

        private fun moveVertically(move: Move) {
            val nextPoint = currentPosition.move(move)
            when (nextPoint.value) {
                WALL -> return
                FREE_SPACE -> {
                    setNewCurrent(nextPoint)
                    return
                }
            }
            val pushedBox = when (nextPoint.value) {
                LARGE_BOX.first() -> listOf(
                    nextPoint,
                    Point(LARGE_BOX.last(), nextPoint.coordinates.copy(x = nextPoint.coordinates.x + 1)),
                )

                else -> listOf(
                    Point(LARGE_BOX.first(), nextPoint.coordinates.copy(x = nextPoint.coordinates.x - 1)),
                    nextPoint,
                )
            }
            val pushedChunks = generateSequence(setOf(pushedBox)) { pushedChunk ->
                pushedChunk
                    .asSequence()
                    .map { chunk -> chunk.filter { it.value != FREE_SPACE } }
                    .filter { it.isNotEmpty() }
                    .flatMap { chunk ->
                        val (left, right) = chunk
                        buildList {
                            with(left.move(move)) {
                                when (value) {
                                    LARGE_BOX.first() -> add(
                                        listOf(this, Point(LARGE_BOX.last(), coordinates.copy(x = coordinates.x + 1))),
                                    ).also { return@buildList }

                                    LARGE_BOX.last() -> add(
                                        listOf(Point(LARGE_BOX.first(), coordinates.copy(x = coordinates.x - 1)), this),
                                    )

                                    else -> add(listOf(this))
                                }
                            }

                            with(right.move(move)) {
                                when (value) {
                                    LARGE_BOX.first() -> add(
                                        listOf(this, Point(LARGE_BOX.last(), coordinates.copy(x = coordinates.x + 1))),
                                    )

                                    else -> add(listOf(this))
                                }
                            }
                        }
                    }.toSet()
            }.takeWhileInclusive { chunks ->
                with(chunks.flatten()) {
                    none { it.value == WALL } && any { it.value in LARGE_BOX }
                }
            }.toList()

            if (pushedChunks.last().flatten().any { it.value == WALL }) return

            pushedChunks.asReversed().forEach { points ->
                points.asSequence().flatten().filter { it.value in LARGE_BOX }.forEach {
                    values[it.coordinates] = FREE_SPACE
                    values[it.coordinates + move] = it.value
                }
            }
            setNewCurrent(nextPoint)
        }
    }

    private fun String.adjustedForPart2() = asSequence().joinToString("") {
        when (it) {
            WALL -> "${WALL}$WALL"
            ROBOT -> "${ROBOT}${FREE_SPACE}"
            BOX -> "${LARGE_BOX.first()}${LARGE_BOX.last()}"
            else -> "${FREE_SPACE}${FREE_SPACE}"
        }
    }

    private data class ParsedInput(
        val grid: List<String>,
        val robotMoves: List<Move>,
    )

    private fun Move.Companion.fromChar(char: Char) = when (char) {
        '<' -> Left
        '>' -> Right
        '^' -> Up
        'v' -> Down
        else -> error("Invalid move char: $char")
    }
}

fun main() {
    val input = readInputLines("Day15")
    println("part1: ${Day15.part1(input)}")
    println("part2: ${Day15.part2(input)}")
}
