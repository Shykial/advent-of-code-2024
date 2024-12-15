package days

import utils.Coordinates
import utils.Move
import utils.MovingPoint
import utils.Regexes
import utils.readInputLines
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter

private typealias Robot = MovingPoint

object Day14 {
    private const val WIDTH = 101
    private const val HEIGHT = 103

    fun part1(input: List<String>): Int {
        val robots = input.map { parseLine(it) }
        val robotsAfter100Seconds = robots.map { robot ->
            generateSequence(robot) { it.makeMove() }.take(WIDTH).last()
        }
        val quadrants = MutableList(4) { 0 }

        robotsAfter100Seconds.forEach {
            val row = when (it.coordinates.y) {
                in (0..<HEIGHT / 2) -> 0
                in (HEIGHT / 2 + 1..HEIGHT) -> 1
                else -> return@forEach
            }
            val column = when (it.coordinates.x) {
                in (0..<WIDTH / 2) -> 0
                in (WIDTH / 2 + 1..WIDTH) -> 1
                else -> return@forEach
            }
            quadrants[row * 2 + column]++
        }

        return quadrants.reduce(Int::times)
    }

    /*
     To actually refine it.
     This is a draft of the solution that just got the job done.
     At first, all pictures were drawn and quickly checked to notice some kind of pattern.
     Some frames had either 2 solid like looking columns at certain position
     or 2 solid like looking rows at certain position.
     After the pattern was seen, only pictures matching an actual pattern were drawn.
     The Solution was observed by quickly looking at those filtered pictures.
     Drawing was as simple as possible - saving txt files.
     I should learn some Compose basics to get such stuff done easier and more conveniently
     */
    fun part2(input: List<String>): Int {
        val newTreesDir = Path("replaced")
        var count = 0
        val initialRobots = input.map { parseLine(it) }
        val pictureSequence = generateSequence(initialRobots) {
            it.map { robot -> robot.makeMove() }
        }.map { robots ->
            Array(HEIGHT) { CharArray(WIDTH) { ' ' } }.apply {
                robots.forEach {
                    this[it.coordinates.y][it.coordinates.x] = 'X'
                }
            }
        }
        pictureSequence.withIndex()
            .filter { (_, picture) ->
                val xCountBeforeCertainColumn = picture.asSequence()
                    .take(37)
                    .sumOf { it.count { it == 'X' } }
                if (xCountBeforeCertainColumn < 40) return@filter true

                val xCountBeforeCertainRow = picture.asSequence()
                    .sumOf { it.asSequence().take(30).count { it == 'X' } }
                xCountBeforeCertainRow < 40
            }.take(10_000)
            .forEach { (index, picture) ->
                println("Processing ${++count}/10_000")
                newTreesDir.resolve("tree_$index.txt").bufferedWriter().use { writer ->
                    picture.forEach {
                        writer.appendLine(it.joinToString(""))
                    }
                }
            }
        TODO("value found ")
    }

    private fun Robot.makeMove() = copy(
        coordinates = Coordinates(
            y = (coordinates.y + move.yShift).mod(HEIGHT),
            x = (coordinates.x + move.xShift).mod(WIDTH),
        ),
    )

    private fun parseLine(line: String): Robot {
        val (px, py, vx, vy) = Regexes.Digits.findAll(line).map { it.value.toInt() }.toList()
        return Robot(
            coordinates = Coordinates(y = py, x = px),
            move = Move(yShift = vy, xShift = vx),
        )
    }
}

fun main() {
    val input = readInputLines("Day14")
    println(Day14.part1(input))
    println(Day14.part2(input))
}
