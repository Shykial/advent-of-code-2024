package days

import utils.chunkedBy
import utils.map
import utils.readInputLines
import utils.splitBy

object Day25 {
    fun part1(input: List<String>): Int = with(parseInput(input)) {
        keySizes.sumOf { key ->
            lockSizes.count { lock ->
                key.zip(lock, Int::plus).all { it < size + 1 }
            }
        }
    }

    private data class ParsedInput(
        val size: Int,
        val keySizes: List<List<Int>>,
        val lockSizes: List<List<Int>>,
    )

    private fun parseInput(input: List<String>): ParsedInput {
        val split = input.splitBy { it.isBlank() }
        val size = split.first().size

        val (lockHeights, inverseKeyHeights) =
            input.splitBy { it.isBlank() }
                .partition { it.first().first() == '#' }
                .map { grid ->
                    grid.map { list ->
                        list.first().indices
                            .map { index -> list.map { it[index] } }
                            .map { value -> value.asSequence().chunkedBy { it }.first().size }
                    }
                }

        return ParsedInput(
            size = size,
            lockSizes = lockHeights,
            keySizes = inverseKeyHeights.map { it.map { h -> size - h } },
        )
    }
}

fun main() {
    val input = readInputLines("Day25")
    println("part1: ${Day25.part1(input)}")
}
