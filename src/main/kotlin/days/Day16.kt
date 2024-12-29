package days

import utils.Coordinates
import utils.Move
import utils.Moves
import utils.MovingPoint
import utils.chunkedBy
import utils.getOrNull
import utils.opposite
import utils.plus
import utils.pointsSequence
import utils.readInputLines
import java.util.PriorityQueue
import kotlin.math.abs

object Day16 {
    private const val START = 'S'
    private const val END = 'E'
    private const val WALL = '#'

    fun part1(input: List<String>): Int = Maze(input).findLowestScore()

    fun part2(input: List<String>): Int = Maze(input).findAllPointsInBestPath().size

    private data class ExploredNode(
        val movingPoint: MovingPoint,
        val totalCost: Int,
    )

    private data class LinkedExploredNode(
        val current: ExploredNode,
        val previous: LinkedExploredNode? = null,
    )

    private data class Maze(private val values: List<String>) {
        private val start = values.pointsSequence().first { it.value == START }.coordinates
        private val end = values.pointsSequence().first { it.value == END }.coordinates

        fun findLowestScore(): Int {
            val nodesToExplore = PriorityQueue<ExploredNode>(compareBy { it.heuristicCost() })
            val visitedPoints = HashMap<MovingPoint, Int>()
            val startingNode = ExploredNode(MovingPoint(start, Move.Right), 0)

            return generateSequence(startingNode) { nodesToExplore.poll() }
                .onEachIndexed { index, bestNode ->
                    visitedPoints[bestNode.movingPoint] = bestNode.totalCost
                    nodesToExplore += bestNode.getNextNodes(index == 0)
                        .filter {
                            val previousCost = visitedPoints[it.movingPoint]
                            previousCost == null || it.totalCost < previousCost
                        }
                }.first { it.movingPoint.coordinates == end }.totalCost
        }

        fun findAllPointsInBestPath(): Set<Coordinates> {
            val nodesToExplore = PriorityQueue<LinkedExploredNode>(compareBy { it.current.heuristicCost() })
            val visitedPoints = HashMap<MovingPoint, Int>()
            val startingNode = LinkedExploredNode(
                current = ExploredNode(movingPoint = MovingPoint(start, Move.Right), totalCost = 0),
                previous = null,
            )
            return generateSequence(startingNode) { nodesToExplore.poll() }
                .onEachIndexed { index, bestNode ->
                    visitedPoints[bestNode.current.movingPoint] = bestNode.current.totalCost
                    nodesToExplore += bestNode.current.getNextNodes(index == 0)
                        .filter {
                            val previousCost = visitedPoints[it.movingPoint]
                            previousCost == null || it.totalCost <= previousCost
                        }.map { LinkedExploredNode(it, bestNode) }
                }.filter { it.current.movingPoint.coordinates == end }
                .chunkedBy { it.current.totalCost }
                .first()
                .flatMap { it.allCoordsSequence() }
                .toSet()
        }

        private fun LinkedExploredNode.allCoordsSequence() =
            generateSequence(this) { it.previous }.map { it.current.movingPoint.coordinates }

        private fun ExploredNode.getNextNodes(isStart: Boolean) =
            Moves.Standard.asSequence()
                .filter { isStart || it != movingPoint.move.opposite() }
                .mapNotNull { move ->
                    val nextCoords = movingPoint.coordinates + move
                    values.getOrNull(nextCoords)
                        ?.takeIf { it != WALL }
                        ?.let { MovingPoint(nextCoords, move) }
                }.map {
                    val totalCostGained = when (it.move) {
                        movingPoint.move -> 1
                        movingPoint.move.opposite() -> 2001
                        else -> 1001
                    }
                    ExploredNode(it, totalCost + totalCostGained)
                }

        private fun ExploredNode.heuristicCost(): Int {
            val distance = abs(movingPoint.coordinates.x - end.x) + abs(movingPoint.coordinates.y - end.y)
            return totalCost + distance
        }
    }
}

fun main() {
    val input = readInputLines("Day16")
    println("part1: ${Day16.part1(input)}")
    println("part2: ${Day16.part2(input)}")
}
