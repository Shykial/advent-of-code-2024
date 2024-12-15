package days

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import utils.LongCoordinates
import utils.Move
import utils.Regexes
import utils.plus
import utils.readInputLines
import utils.splitBy
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.hypot

object Day13 {
    fun part1(input: List<String>): Int = runBlocking(Dispatchers.Default) {
        parseInput(input).map { async { it.calculateMinPriceUsingAStar() ?: 0 } }
            .awaitAll()
            .sum()
    }

    fun part2(input: List<String>): Long =
        parseInput(input)
            .map { it.adjustedForPart2() }
            .sumOf { it.calculateMinPriceSolvingLinearEquation() ?: 0 }

    private fun ClawMachine.calculateMinPriceUsingAStar(): Int? {
        val startingNode = ExploredNode(LongCoordinates(0, 0), 0)
        val nodesToExplore = PriorityQueue<ExploredNode>(compareBy { it.heuristicValue(prize) })
        val visitedNodes = mutableSetOf<ExploredNode>()

        return generateSequence(startingNode) { nodesToExplore.poll() }.onEach { bestNode ->
            visitedNodes += bestNode
            nodesToExplore += listOf(
                bestNode.copy(
                    location = bestNode.location + buttonA,
                    totalCost = bestNode.totalCost + 3,
                ),
                bestNode.copy(
                    location = bestNode.location + buttonB,
                    totalCost = bestNode.totalCost + 1,
                ),
            ).filter { (it.location.x <= prize.x && it.location.y <= prize.y) && it !in visitedNodes }
        }.firstOrNull { it.location == prize }?.totalCost
    }

    private fun ClawMachine.calculateMinPriceSolvingLinearEquation(): Long? {
        val xSum = prize.x * buttonB.yShift
        val ySum = prize.y * buttonB.xShift

        val aCount = buttonB.yShift * buttonA.xShift - buttonB.xShift * buttonA.yShift
        val a = (xSum - ySum) / aCount.toDouble()
        val b = (prize.x - buttonA.xShift * a) / buttonB.xShift
        return when {
            a % 1 == 0.0 && b % 1 == 0.0 -> (a * 3 + b).toLong()
            else -> null
        }
    }

    private data class ExploredNode(
        val location: LongCoordinates,
        val totalCost: Int,
    )

    private fun ExploredNode.heuristicValue(goal: LongCoordinates) = location.distanceFrom(goal) + totalCost

    private fun LongCoordinates.distanceFrom(other: LongCoordinates) =
        hypot(abs(x.toDouble() - other.x), abs(y.toDouble() - other.y))

    private data class ClawMachine(
        val buttonA: Move,
        val buttonB: Move,
        val prize: LongCoordinates,
    )

    private fun parseInput(input: List<String>): List<ClawMachine> =
        input
            .splitBy { it.isBlank() }
            .map { lines ->
                val (a, b, prize) = lines.map { line ->
                    Regexes.NonNegativeDigits.findAll(line).map { it.value.toInt() }.toList()
                }
                ClawMachine(
                    buttonA = Move(xShift = a.first(), yShift = a.last()),
                    buttonB = Move(xShift = b.first(), yShift = b.last()),
                    prize = LongCoordinates(x = prize.first().toLong(), y = prize.last().toLong()),
                )
            }

    private fun ClawMachine.adjustedForPart2() =
        copy(prize = LongCoordinates(y = prize.y + 10000000000000, x = prize.x + 10000000000000))
}

fun main() {
    val input = readInputLines("Day13")
    println(Day13.part1(input))
    println(Day13.part2(input))
}
