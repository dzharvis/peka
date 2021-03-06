package com.dzharvis.peka

import com.dzharvis.components.*
import utils.binToDec
import utils.bits
import utils.initializeMemory
import utils.ss

private val Halt = sig(1)
private val MemRegI = sig(1)
private val MemI = sig(1)
private val MemO = sig(1)
private val InstRegO = sig(1)
private val InstRegI = sig(1)
private val ARegI = sig(1)
private val ARegO = sig(1)
private val FlagSet = sig(1)
private val AluO = sig(1)
private val Subst = sig(1)
private val BRegI = sig(1)
private val OutRegI = sig(1)
private val CntE = sig(1)
private val CntO = sig(1)
private val CntI = sig(1)
private val CntCl = sig(1)
private val BRegO = sig(1)

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
                FlagSet + AluO + Subst + BRegI + OutRegI + CntE + CntO + CntI
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


var memOn = sig(1)

fun initMemory(clk: Signals, memReg: Signals, bus: Signals) {
    val memDirectOut = sig(8)
    val wr = sig(1)
    val rd = MemO
    val memin = clk + wr + memReg + bus
    memory8Bit(memin, memDirectOut, 8)
    val mm = memReg.remember()
    val program = compile(code)
    initializeMemory(memin, memDirectOut, program.steps.map { it.addr }, program.steps.map { it.value })
    AND(MemI + memOn, wr)
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
    connectToBus(InstRegO, bus, directOut.ss(0..3) + sig(4))

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
    initMemory(clk, memRegDirectOut, bus)
    val (aluDirectOutp, aluCarryBit) = initAlu(bus, aRegDirectOut, bRegDirectOut)
    val flagRegDirectOutp = initFlagRegister(clk, aluDirectOutp, aluCarryBit)
    initControlUnit(clk, instRegDirectOut, flagRegDirectOutp)

    memOn.forceUpdate(1)
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
    var i = 0
    while (Halt[0].signal == 0) {
        println("------- step $i")
        clk.forceUpdate(1)
        clk.forceUpdate(0)
        printLeds()
        println("------- step $i")
        i++
    }

    println("Final result: " + binToDec(outpRegDirectOut.bits()))
}

fun printLeds() = leds.forEach { it.prnt() }
