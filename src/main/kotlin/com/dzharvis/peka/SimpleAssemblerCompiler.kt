package com.dzharvis.peka

import utils.decToBin


val code = """
    LDI 15
    STA 14
    
    LDI 1
    STA 15
    
    LDA 14
    ADD 15
    OUT
    HLT
""".trimIndent()

fun compile(code: String): Instructions {
    val steps = code.split("\n")
        .filter { !it.startsWith("#") }
        .filter { it.isNotBlank() }
        .mapIndexed { i, codeStr ->
            val instrAndCode = codeStr.split("[\\s]+".toRegex())
            val instr = instrAndCode[0]
            val value = instrAndCode.getOrNull(1)
            val instrCode = instrCodes[instr]!!
            val data = decToBin(value?.toInt() ?: 0, 4)
            InstructionStep(addr = decToBin(i, 8), value = data + instrCode)
        }
    return Instructions(steps)
}

val instrCodes = mapOf(
    "LDA" to decToBin(1, 4),
    "ADD" to decToBin(2, 4),
    "SUB" to decToBin(3, 4),
    "STA" to decToBin(4, 4),
    "LDI" to decToBin(5, 4),
    "JMP" to decToBin(6, 4),
    "JC" to decToBin(7, 4),
    "JZ" to decToBin(8, 4),
    "OUT" to decToBin(14, 4),
    "HLT" to decToBin(15, 4)
)


fun main() {
    val instructions = compile(code)
    instructions.steps.forEach {
        println(it)
    }
//    println(instructions)
}
