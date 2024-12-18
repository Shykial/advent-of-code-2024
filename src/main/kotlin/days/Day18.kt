package days

import utils.Coordinates
import utils.Moves
import utils.getOrNull
import utils.plus
import utils.readInputLines
import utils.set
import java.util.PriorityQueue

object Day18 {
    private const val SIZE = 71
    private const val SAFE = '.'
    private const val CORRUPTED = '#'
    private const val BYTES_FALLEN = 1024

    fun part1(input: List<String>): Int =
        input.map { parseLine(it) }
            .let(::createMemoryGrid)
            .getFastestSolutionOrNull()!!

    fun part2(input: List<String>): String {
        val fallenBytes = input.map { parseLine(it) }
        val grid = createMemoryGrid(fallenBytes)
        return fallenBytes.asSequence().drop(BYTES_FALLEN)
            .onEach { coordinates -> grid.values[coordinates] = CORRUPTED }
            .first { grid.getFastestSolutionOrNull() == null }
            .let { "${it.x},${it.y}" }
    }

    private data class MutableMemoryGrid(val values: List<CharArray>) {
        private val endCoordinates = Coordinates(SIZE - 1, SIZE - 1)

        fun getFastestSolutionOrNull(): Int? {
            val start = ExploredNode(Coordinates(0, 0), 0)
            val visitedPoints = HashSet<Coordinates>()
            val nodesToExplore = PriorityQueue<ExploredNode>(compareBy { it.totalCost })

            return generateSequence(start) { nodesToExplore.poll() }
                .onEach { bestNode ->
                    visitedPoints += bestNode.coordinates
                    nodesToExplore += bestNode.getNextNodes().filter { it.coordinates !in visitedPoints }
                }.firstOrNull { it.coordinates == endCoordinates }?.movesSoFar
        }


        private fun ExploredNode.getNextNodes() =
            Moves.Standard.asSequence()
                .mapNotNull { move -> (coordinates + move).takeIf { values.getOrNull(it) == SAFE } }
                .map { ExploredNode(it, movesSoFar + 1) }

        private data class ExploredNode(
            val coordinates: Coordinates,
            val movesSoFar: Int
        ) {
            val totalCost = (SIZE - 1 - coordinates.y) + (SIZE - 1 - coordinates.x) + movesSoFar
        }
    }

    private fun createMemoryGrid(fallenBytes: List<Coordinates>): MutableMemoryGrid {
        val initialGrid = List(SIZE) { CharArray(SIZE) { SAFE } }
        fallenBytes.asSequence()
            .take(BYTES_FALLEN)
            .forEach { initialGrid[it] = CORRUPTED }

        return MutableMemoryGrid(initialGrid)
    }

    private fun parseLine(line: String): Coordinates {
        val (x, y) = line.split(',').map { it.toInt() }
        return Coordinates(y = y, x = x)
    }
}

fun main() {
    val input = readInputLines("Day18")

    println("part1: ${Day18.part1(input)}")
    println("part2: ${Day18.part2(input)}")
}