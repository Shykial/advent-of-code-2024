package days

import utils.Regexes
import utils.readInputLines
import kotlin.math.pow

object Day17 {
    fun part1(input: List<String>): String = parseComputer(input).output().joinToString(",")

    fun part2(input: List<String>): Long = with(parseComputer(input)) {
        findLowestRegisterValue(
            currentRegister = 8.0.pow(program.size - 1).toLong(),
            currentIndex = program.lastIndex,
        )!!
    }

    private fun Computer.findLowestRegisterValue(currentRegister: Long, currentIndex: Int): Long? {
        val filteringSequence =
            generateSequence(currentRegister) { it + 8.0.pow(currentIndex).toLong() }
                .map {
                    it to Computer(
                        registerA = it,
                        registerB = registerB,
                        registerC = registerC,
                        program = program,
                    ).output()
                }.takeWhile { (_, output) ->
                    currentIndex == program.lastIndex || output[currentIndex + 1] == program[currentIndex + 1].toLong()
                }.filter { (_, output) -> output[currentIndex] == program[currentIndex].toLong() }

        return when (currentIndex) {
            0 -> filteringSequence.firstOrNull()?.first
            else -> filteringSequence.firstNotNullOfOrNull {
                findLowestRegisterValue(
                    currentRegister = it.first,
                    currentIndex = currentIndex - 1,
                )
            }
        }
    }

    private fun parseComputer(input: List<String>): Computer {
        val (aLong, bLong, cLong, program) =
            input
                .filter { it.isNotBlank() }
                .map { line -> Regexes.Digits.findAll(line).map { it.value.toLong() } }

        return Computer(
            registerA = aLong.single(),
            registerB = bLong.single(),
            registerC = cLong.single(),
            program = program.map { it.toInt() }.toList(),
        )
    }

    class Computer(
        var registerA: Long,
        var registerB: Long,
        var registerC: Long,
        val program: List<Int>,
    ) {
        fun output(): List<Long> = buildList {
            var instructionPointer = 0
            while (instructionPointer < program.size) {
                val opCode = program[instructionPointer]
                val operand = program[instructionPointer + 1]
                when (opCode) {
                    0 -> registerA = (registerA / (2.0.pow(comboOperandValue(operand).toDouble()))).toLong()
                    1 -> registerB = registerB xor operand.toLong()
                    2 -> registerB = comboOperandValue(operand) % 8
                    3 -> if (registerA != 0L) {
                        instructionPointer = operand
                        continue
                    }

                    4 -> registerB = registerB xor registerC
                    5 -> this += comboOperandValue(operand) % 8
                    6 -> registerB = (registerA / (2.0.pow(comboOperandValue(operand).toDouble()))).toLong()
                    7 -> registerC = (registerA / (2.0.pow(comboOperandValue(operand).toDouble()))).toLong()
                }
                instructionPointer += 2
            }
        }

        private fun comboOperandValue(comboOperand: Int): Long = when (comboOperand) {
            in (0..3) -> comboOperand.toLong()
            4 -> registerA
            5 -> registerB
            6 -> registerC
            else -> error("Invalid combo operand: $comboOperand")
        }
    }
}

fun main() {
    val input = readInputLines("Day17")
    println("part1: ${Day17.part1(input)}")
    println("part2: ${Day17.part2(input)}")
}
