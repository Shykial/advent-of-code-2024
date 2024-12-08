package days

import utils.Coordinates
import utils.contains
import utils.minus
import utils.pairCombinations
import utils.plus
import utils.pointsSequence
import utils.readInputLines

object Day08 {
    private val antennaChars = (('a'..'z') + ('A'..'Z') + ('0'..'9')).toSet()

    fun part1(input: List<String>): Int = input.countUniqueAntinodes { (first, second) ->
        val diff = second - first
        sequenceOf(first - diff, second + diff).filter { it in input }
    }

    fun part2(input: List<String>): Int = input.countUniqueAntinodes { (first, second) ->
        val diff = second - first
        val firstSeq = generateSequence(first) { it - diff }.takeWhile { it in input }
        val secondSeq = generateSequence(second) { it + diff }.takeWhile { it in input }
        firstSeq + secondSeq
    }

    private fun List<String>.countUniqueAntinodes(
        antinodesMapping: (antennasPair: Pair<Coordinates, Coordinates>) -> Sequence<Coordinates>,
    ) = this.pointsSequence()
        .filter { it.value in antennaChars }
        .groupBy({ it.value }, { it.coordinates })
        .values
        .flatMap { it.pairCombinations().flatMap(antinodesMapping) }
        .distinct()
        .count()
}

fun main() {
    val input = readInputLines("Day08")
    println("part1: ${Day08.part1(input)}")
    println("part2: ${Day08.part2(input)}")
}
