package days

import utils.Coordinates
import utils.Moves
import utils.getOrNull
import utils.plus
import utils.readInputLines
import utils.set
import java.util.LinkedList
import java.util.PriorityQueue

object Day18 {
    private const val SIZE = 71
    private const val SAFE = '.'
    private const val CORRUPTED = '#'
    private const val BYTES_FALLEN = 1024

    fun part1(input: List<String>): Int =
        input.map { parseLine(it) }
            .let(::createMemoryGrid)
            .getSolutionCoords().size

    fun part2(input: List<String>): String {
        val fallenBytes = input.map { parseLine(it) }
        val grid = createMemoryGrid(fallenBytes)
        val seenSolutions = LinkedList<Set<Coordinates>>()
        return fallenBytes.asSequence()
            .drop(BYTES_FALLEN)
            .first { coords ->
                grid.values[coords] = CORRUPTED
                seenSolutions.removeAll { coords in it }
                seenSolutions.isEmpty() && grid.getSolutionCoords().also { seenSolutions += it }.isEmpty()
            }.let { "${it.x},${it.y}" }
    }

    private data class MutableMemoryGrid(val values: List<CharArray>) {
        private val endCoordinates = Coordinates(SIZE - 1, SIZE - 1)

        fun getSolutionCoords(): Set<Coordinates> {
            val start = LinkedExploredNode(ExploredNode(Coordinates(0, 0), 0), null)
            val visitedPoints = HashSet<Coordinates>()
            val nodesToExplore = PriorityQueue<LinkedExploredNode>(compareBy { it.current.totalCost })

            val winningNode = generateSequence(start) { nodesToExplore.poll() }
                .onEach { bestNode ->
                    nodesToExplore += bestNode.getNextNodes().filter { it.current.coordinates !in visitedPoints }
                    visitedPoints += bestNode.current.coordinates
                }.firstOrNull { it.current.coordinates == endCoordinates }

            return generateSequence(winningNode) { it.previous }.map { it.current.coordinates }.toSet()
        }

        private fun LinkedExploredNode.getNextNodes() =
            Moves.Standard.asSequence()
                .mapNotNull { move -> (current.coordinates + move).takeIf { values.getOrNull(it) == SAFE } }
                .map { LinkedExploredNode(ExploredNode(it, current.movesSoFar + 1), this) }

        private data class LinkedExploredNode(
            val current: ExploredNode,
            val previous: LinkedExploredNode? = null,
        )

        private data class ExploredNode(
            val coordinates: Coordinates,
            val movesSoFar: Int,
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
