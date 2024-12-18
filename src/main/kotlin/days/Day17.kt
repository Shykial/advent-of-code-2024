package days

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.Regexes
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.pow

private const val PRINT_EVERY_N = 100_000_000L

object Day17 {
    fun part1(input: List<String>): String = parseInput(input).outputSequence().joinToString(",")

    fun part2MultiThread(input: List<String>): Long = runBlocking(Dispatchers.Default) {
        val initialComputer = parseInput(input)
        val concurrent = Runtime.getRuntime().availableProcessors()
        val result = AtomicLong()
        coroutineScope {
            (1L..concurrent).forEach { number ->
                launch {
//                    runInterruptible {
                    generateSequence(number) { it + concurrent }
                        .onEach { ensureActive() }
                        .first { registerA ->
                            val computer = Computer(
                                registerA = registerA,
                                registerB = initialComputer.registerB,
                                registerC = initialComputer.registerC,
                                program = initialComputer.program,
                            )
                            val iterator = computer.outputSequence().iterator()
                            computer.program.all {
                                iterator.hasNext() && iterator.next() == it
                            }.also {
                                if (registerA % PRINT_EVERY_N == 0L) {
                                    println("Processed register: $registerA")
                                }
                            }
                        }.let {
//                            println("Match found!")
                            result.set(it)
                            coroutineContext[Job]!!.parent!!.cancelChildren()
//                                error("asdfadsf")
                        }
//                    }
                }
            }
        }
        result.get()
    }

//    fun part2Something(input: List<String>): Long {
//        val seen = mutableMapOf<String, MutableList<Long>>()
//        val initialComputer = parseInput(input)
//        return generateSequence(1L) { it + 1 }
//            .first { registerA ->
//                val computer = Computer(
//                    registerA = registerA,
//                    registerB = initialComputer.registerB,
//                    registerC = initialComputer.registerC,
//                    program = initialComputer.program
//                )
//                val output = computer.output()
//                seen.getOrPut(output.joinToString(",")) { mutableListOf() } += registerA
//                (output == computer.program).also {
//                    if (registerA % 100_000 == 0L) {
//                        println()
//                    }
//                }
// //                val allMatch = computer.program.asSequence()
// //                    .zip(computer.outputSequence())
// //                    .onEach { counter++ }
// //                    .all { (a, b) -> a == b }
// //                allMatch && counter == computer.program.size
//            }
//
//    }

    fun part2SingleThread(input: List<String>): Long {
        val initialComputer = parseInput(input)
        return generateSequence(117440L) { it + 1 }
            .first { registerA ->
                val computer = Computer(
                    registerA = registerA,
                    registerB = initialComputer.registerB,
                    registerC = initialComputer.registerC,
                    program = initialComputer.program,
                )
                val iterator = computer.outputSequence().iterator()
                computer.program.all {
                    iterator.hasNext() && iterator.next() == it
                }.also {
                    if (registerA % PRINT_EVERY_N == 0L) {
                        println("Processed register: $registerA")
                    }
                }

//                val allMatch = computer.program.asSequence()
//                    .zip(computer.outputSequence())
//                    .onEach { counter++ }
//                    .all { (a, b) -> a == b }
//                allMatch && counter == computer.program.size
            }.also {
                println()
            }
    }

//    fun part2Normal(input: List<String>): Long {
//        val initialComputer = parseInput(input)
// //        val seen = mutableMapOf<String, MutableList<Long>>()
//
//        return generateSequence(1L) { it + 1 }
//            .map {
//                Computer(
//                    registerA = it,
//                    registerB = initialComputer.registerB,
//                    registerC = initialComputer.registerC,
//                    program = initialComputer.program
//                )
//            }.firstNotNullOf { computer ->
//                val initialRegisterValue = computer.registerA
//                val output = computer.outputSequence().toList()
// //                seen.getOrPut(output.joinToString(",")) { mutableListOf() }.add(initialRegisterValue)
//                initialRegisterValue.takeIf { output == computer.program }
//            }
//    }

    private fun parseInput(input: List<String>): Computer {
        val (aLong, bLong, cLong, program) =
            input
                .filter { it.isNotBlank() }
                .map { line -> Regexes.Digits.findAll(line).map { it.value.toLong() } }

        return Computer(
            registerA = aLong.single(),
            registerB = bLong.single(),
            registerC = cLong.single(),
            program = program.map { it.toInt() }.toList().toIntArray(),
        )
    }

    class Computer(
        var registerA: Long,
        var registerB: Long,
        var registerC: Long,
        val program: IntArray,
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

        fun outputSequence(): Sequence<Int> = sequence {
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
                    5 -> yield((comboOperandValue(operand) % 8).toInt())
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
/*
xcept for jump instructions, the instruction pointer increases by 2 after each instruction is processed (to move past the instruction's opcode and its operand)
If the computer tries to read an opcode past the end of the program, it instead halts.
The adv instruction (opcode 0) performs division.
 */

// part 2 for sure more than 100200000000
fun main() {
//    val input = readInputLines("Day17")

//    val input = """
//        Register A: 729
//        Register B: 0
//        Register C: 0
//
//        Program: 0,1,5,4,3,0
//    """.trimIndent().lines()

    val input = """
    Register A: 2024
    Register B: 0
    Register C: 0

    Program: 0,3,5,4,3,0
    """.trimIndent().lines()
    // 117440

    println("part1: ${Day17.part1(input)}")
//    println("part2: ${Day17.part2Normal(input)}")
//    println("part2: ${Day17.part2SingleThread(input)}")
//    println("part2: ${Day17.part2Something(input)}")
    println("part2: ${Day17.part2MultiThread(input)}")
//    println("part2: ${Day17.part2SingleThreadOld(input)}")
//    println("part2: ${Day17.part2MultithreadedNew(input)}")
//    println("part2: ${Day17.part2Test(input)}")
}
