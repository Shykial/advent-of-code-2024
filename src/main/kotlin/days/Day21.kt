package days

import utils.Coordinates
import utils.Move
import utils.Moves
import utils.chunkedBy
import utils.distanceFrom
import utils.plus
import utils.pointsSequence
import utils.readInputLines
import java.util.PriorityQueue

object Day21 {
    private val shortestPathsCache = mutableMapOf<Triple<Char, Char, KeyPad>, List<String>>()
    private val steeringPadCombinationCountsCache = mutableMapOf<Triple<Char, Char, Int>, Long>()

    fun part1(input: List<String>): Long = input.sumOf { line ->
        countComplexitiesSum(line, numberOfSteeringRobots = 2)
    }

    fun part2(input: List<String>): Long = input.sumOf { line ->
        countComplexitiesSum(line, numberOfSteeringRobots = 25)
    }

    private fun countComplexitiesSum(
        line: String,
        numberOfSteeringRobots: Int,
    ): Long {
        val numPadCombinations = "A$line".zipWithNext { a, b ->
            findShortestPathsToTypeKey(fromKey = a, toKey = b, keyPad = KeyPad.NUMERIC)
        }
        val totalCost = numPadCombinations.sumOf { combinations ->
            combinations.minOf { it.countSteeringCombinations(1, numberOfSteeringRobots) }
        }
        return totalCost * line.dropLast(1).toLong()
    }

    private fun String.countSteeringCombinations(level: Int, maxLevel: Int) =
        "A$this"
            .zipWithNext { a, b -> findAllBetweenChars(a, b, level, maxLevel) }
            .sum()

    private fun findAllBetweenChars(firstChar: Char, secondChar: Char, level: Int, maxLevel: Int): Long =
        steeringPadCombinationCountsCache.getOrPut(Triple(firstChar, secondChar, maxLevel - level)) {
            val combinations = findShortestPathsToTypeKey(firstChar, secondChar, KeyPad.STEERING)

            when (level) {
                maxLevel -> combinations.first().length.toLong()
                else -> combinations.minOf { it.countSteeringCombinations(level = level + 1, maxLevel = maxLevel) }
            }
        }

    private fun findShortestPathsToTypeKey(
        fromKey: Char,
        toKey: Char,
        keyPad: KeyPad,
    ): List<String> = shortestPathsCache.getOrPut(Triple(fromKey, toKey, keyPad)) {
        leadingMovesSequence(fromKey, toKey, keyPad)
            .map { it + 'A' }
            .chunkedBy { it.length }
            .first()
    }

    private fun leadingMovesSequence(
        fromKey: Char,
        toKey: Char,
        keyPad: KeyPad,
    ): Sequence<String> {
        val startingLocation = keyPad.locationsByValue[fromKey]!!
        val endLocation = keyPad.locationsByValue[toKey]!!

        val startingNode = ExploredNode(coordinates = startingLocation, moves = "")
        val nodesToExplore = PriorityQueue<ExploredNode>(compareBy { it.heuristicCost(endLocation) })
        return generateSequence(startingNode) { nodesToExplore.poll() }
            .onEach { bestNode ->
                nodesToExplore += Moves.Standard.asSequence()
                    .mapNotNull { move ->
                        val nextCoords = bestNode.coordinates + move
                        keyPad.valuesByLocation[nextCoords]
                            ?.takeIf { it != '#' }
                            ?.let { ExploredNode(coordinates = nextCoords, moves = bestNode.moves + move.toChar()) }
                    }
            }.filter { it.coordinates == endLocation }
            .map { it.moves }
    }

    private data class ExploredNode(val coordinates: Coordinates, val moves: String)

    private fun ExploredNode.heuristicCost(endCoordinates: Coordinates) =
        moves.length + coordinates.distanceFrom(endCoordinates)

    enum class KeyPad(values: List<String>) {
        NUMERIC(listOf("789", "456", "123", "#0A")),
        STEERING(listOf("#^A", "<v>")),
        ;

        val locationsByValue = values.pointsSequence().associate { it.value to it.coordinates }
        val valuesByLocation = locationsByValue.entries.associate { it.value to it.key }
    }

    private fun Move.toChar() = when (this) {
        Move.Up -> '^'
        Move.Down -> 'v'
        Move.Right -> '>'
        Move.Left -> '<'
        else -> error("Invalid move: $this")
    }
}

fun main() {
    val input = readInputLines("Day21")
    println("part1: ${Day21.part1(input)}")
    println("part2: ${Day21.part2(input)}")
}
