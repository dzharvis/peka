package com.dzharvis.peka

import com.dzharvis.components.*
import utils.binToDec
import utils.bits
import utils.initializeMemory
import utils.ss

val Halt = sig(1)
val MemRegI = sig(1)
val MemI = sig(1)
val MemO = sig(1)
val CntE = sig(1)
val CntO = sig(1)
val CntI = sig(1)
val CntCl = sig(1)
val InstRegI = sig(1)
val InstRegO = sig(1)
val ARegI = sig(1)
val ARegO = sig(1)
val BRegI = sig(1)
val BRegO = sig(1)
val Subst = sig(1)
val AluO = sig(1)
val OutRegI = sig(1)
val FlagSet = sig(1)

fun initCounter(clk: Signals, bus: Signals) {
    val clear = CntCl
    val ci = CntI
    val ce = CntE
    val counterIn = clear + ci + ce + clk + bus.ss(0..3)
    val counterDirectOur = sig(4)
    syncCounterWithEnable(counterIn, counterDirectOur)
    LED(counterDirectOur, "COUNTER DIRECT OUT")
    CntE.forceUpdate(0)
    CntO.forceUpdate(0)
    CntCl.forceUpdate(0)
    connectToBus(CntO, bus.ss(0..3), counterDirectOur)
}

fun initControlUnit(
    clk: Signals,
    instrRegOutput: Signals,
    flagRegDirectOutp: Signals
) {
    val controllerOut =
        Halt + MemRegI + MemI + MemO + InstRegO + InstRegI + ARegI + ARegO +
                FlagSet + AluO + Subst + BRegI + OutRegI + CntE + CntO + CntCl
    LED(controllerOut, "controller")
    controller(clk + instrRegOutput.ss(4..7) + flagRegDirectOutp, controllerOut)
}

fun initFlagRegister(clk: Signals, aluDirectOut: Signals, aluCarryBit: Signals): Signals {
    val flagDirectOut = sig(8)
    LED(flagDirectOut, "FLAG REG OUR")
    val aluAllZeroes = aluDirectOut
        .chunked(2)
        .map { inp ->
            NOR(inp, sig(1)).output[0]
        }.let {
            val outp = sig(1)
            andn(it, outp, it.size)
            outp
        }
    val flagInp = aluCarryBit + aluAllZeroes + sig(6)
    LED(flagInp, "FLAG REG INP")
    register(clk + FlagSet + flagInp, flagDirectOut)
    return flagDirectOut.ss(0..1)
}

fun initAlu(
    bus: Signals,
    aRegDirectOut: Signals,
    bRegDirectOut: Signals
): Pair<Signals, Signals> {
    val aluDirectOut = sig(8)
    LED(aluDirectOut, "ALU")
    val carryBit = sig(1)
    alu(Subst + aRegDirectOut + bRegDirectOut, aluDirectOut + carryBit)
    connectToBus(AluO, bus, aluDirectOut)
    return aluDirectOut to carryBit
}


val memoryAddress = listOf(
    listOf(0, 0, 0, 0, 0, 0, 0, 0).reversed(),
    listOf(0, 0, 0, 0, 0, 0, 0, 1).reversed(),
    listOf(0, 0, 0, 0, 0, 0, 1, 0).reversed(),
    listOf(0, 0, 0, 0, 1, 1, 1, 0).reversed(),
    listOf(0, 0, 0, 0, 1, 1, 1, 1).reversed()
)

val memoryValue = listOf(
    listOf(0, 1, 1, 1, 1, 0, 0, 0), // ASM: [LDA 14]
    listOf(1, 1, 1, 1, 0, 1, 0, 0), // ASM: [ADD 15]
    listOf(0, 0, 0, 0, 0, 1, 1, 1), // ASM: [OUT]
    listOf(0, 0, 1, 1, 1, 0, 0, 0), // DATA: [28]
    listOf(0, 1, 1, 1, 0, 0, 0, 0)  // DATA: [14]

)

fun initMemory(memReg: Signals, bus: Signals) {
    val memDirectOut = sig(8)
    val wr = MemI
    val rd = MemO
    val memin = wr + rd + memReg + memDirectOut
    memory8Bit(memin, memDirectOut, 8)
    val mm = memReg.remember()
    initializeMemory(memin, memoryAddress, memoryValue)
    memReg.forceUpdate(*mm)
    LED(memReg, "RAM ADDR")
    LED(memDirectOut, "RAM OUT")
    connectToBus(rd, bus, memDirectOut)
}

// first half - instruction code, second half - data or addr
fun initInstrReg(clk: Signals, bus: Signals): Signals {
    val directOut = sig(8)
    val en = InstRegI
    register(clk + en + bus, directOut)
    connectToBus(InstRegO, bus, directOut)

    return directOut
}

fun initMemReg(clk: Signals, bus: Signals): Signals {
    val directOut = sig(8)
    val en = MemRegI
    register(clk + en + bus.ss(0..3) + sig(4), directOut)

    return directOut
}

fun initAReg(clk: Signals, bus: Signals): Signals {
    val directOut = sig(8)
    register(clk + ARegI + bus, directOut)
    connectToBus(ARegO, bus, directOut)
    return directOut
}

fun initBReg(clk: Signals, bus: Signals): Signals {
    val directOut = sig(8)
    register(clk + BRegI + bus, directOut)
    connectToBus(BRegO, bus, directOut)
    return directOut
}

fun initOutReg(clk: Signals, bus: Signals): Signals {
    val directOut = sig(8)
    register(clk + OutRegI + bus, directOut)
    return directOut
}

fun connectToBus(en: Signals, bus: Signals, out: Signals) {
    out.forEachIndexed { i, s ->
        TriStateGate(listOf(s) + en, bus.ss(i))
    }
}

fun initPeka() {
    val (clk, bus) = sigs(1, 8)
    val instRegDirectOut = initInstrReg(clk, bus)
    val memRegDirectOut = initMemReg(clk, bus)
    val aRegDirectOut = initAReg(clk, bus)
    val bRegDirectOut = initBReg(clk, bus)
    val outpRegDirectOut = initOutReg(clk, bus)

    initCounter(clk, bus)
    initMemory(memRegDirectOut, bus)
    val (aluDirectOutp, aluCarryBit) = initAlu(bus, aRegDirectOut, bRegDirectOut)
    val flagRegDirectOutp = initFlagRegister(clk, aluDirectOutp, aluCarryBit)
    initControlUnit(clk, instRegDirectOut, flagRegDirectOutp)

    LED(memRegDirectOut, "MEM REG")
    LED(outpRegDirectOut, "OUTPUT")
    LED(instRegDirectOut, "INST REG")
    LED(aRegDirectOut, "A REG")
    LED(bRegDirectOut, "B REG")
    LED(bus, "BUS")
    LED(clk, "CLOCK")

    clkReset!!.forceUpdate(1)
    CntCl.forceUpdate(1)

    clk.forceUpdate(1)
    clk.forceUpdate(0)

    clkReset!!.forceUpdate(0)
    CntCl.forceUpdate(0)
    CntI.forceUpdate(0)

    printLeds()
    println("start update")
    for (i in 0..12) {
        println("------- step $i")
        clk.forceUpdate(1)
        clk.forceUpdate(0)
        printLeds()
        println("------- step $i")
    }

    println("Final result: " + binToDec(outpRegDirectOut.bits()))
}

fun printLeds() = leds.forEach { it.prnt() }
