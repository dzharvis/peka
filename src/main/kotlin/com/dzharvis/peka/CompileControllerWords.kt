package com.dzharvis.peka

import utils.decToBin

private val Halt = 1
private val MemRegI = 1 shl 1
private val MemI = 1 shl 2
private val MemO = 1 shl 3
private val InstRegO = 1 shl 4
private val InstRegI = 1 shl 5
private val ARegI = 1 shl 6
private val ARegO = 1 shl 7
private val FlagSet = 1 shl 8
private val AluO = 1 shl 9
private val Subst = 1 shl 10
private val BRegI = 1 shl 11
private val OutRegI = 1 shl 12
private val CntE = 1 shl 13
private val CntO = 1 shl 14
private val CntI = 1 shl 15

data class InstructionStep(val addr: List<Int>, val value: List<Int>)
data class Instruction(val steps: List<InstructionStep>)

fun compileInstruction(instrCode: Int, step: List<Int>, flagsIn: List<List<Int>>, words: List<Int>): Instruction {
    val instr = if (instrCode == -1) listOf(-1, -1, -1, -1) else decToBin(instrCode, 4)
    val steps = words.mapIndexed { i, word ->
        val step = decToBin(step[i], 4)
        val flags = flagsIn[i]
        val outputWord = decToBin(word, 16)
        InstructionStep(step + instr + flags, outputWord)
    }
    return Instruction(steps)
}


fun compile(): List<Instruction> {
    val anyFlags = listOf(listOf(-1, -1), listOf(-1, -1), listOf(-1, -1))
    val defaultSteps = listOf(2, 3, 4)
    val FETCH = compileInstruction(-1, listOf(0, 1), anyFlags, listOf(MemRegI or CntO, MemO or InstRegI or CntE))
    val LDA = compileInstruction(1, defaultSteps, anyFlags, listOf(MemRegI or InstRegO, MemO or ARegI, 0))
    val ADD = compileInstruction(2, defaultSteps, anyFlags, listOf(MemRegI or InstRegO, MemO or BRegI, ARegI or AluO or FlagSet))
    val SUB = compileInstruction(3, defaultSteps, anyFlags, listOf(MemRegI or InstRegO, MemO or BRegI, ARegI or AluO or Subst or FlagSet))
    val STA = compileInstruction(4, defaultSteps, anyFlags, listOf(MemRegI or InstRegO, MemI or ARegO, 0))
    val LDI = compileInstruction(5, defaultSteps, anyFlags, listOf(MemO or ARegI, 0, 0))
    val JMP = compileInstruction(6, defaultSteps, anyFlags, listOf(MemO or ARegI, 0, 0))
    val JC =  compileInstruction(7, listOf(2, 2, 3, 4), listOf(listOf(0, -1), listOf(1, -1), listOf(-1, -1), listOf(-1, -1)), listOf(0, MemO or CntI, 0, 0))
    val JZ =  compileInstruction(8, listOf(2, 2, 3, 4), listOf(listOf(-1, 0), listOf(-1, 1), listOf(-1, -1), listOf(-1, -1)), listOf(0, MemO or CntI, 0, 0))
    val OUT = compileInstruction(14, defaultSteps, anyFlags, listOf(ARegO or OutRegI, 0, 0))
    val HLT = compileInstruction(15, defaultSteps, anyFlags, listOf(Halt, 0, 0))

    return listOf(FETCH, LDA ,ADD ,SUB ,STA ,LDI ,JMP ,JC ,JZ ,OUT ,HLT)
}

fun main() {
    compile()
}