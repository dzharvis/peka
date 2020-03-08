package com.dzharvis.peka

import com.dzharvis.components.*
import com.dzharvis.peka.Controls.*
import utils.initializeMemory
import utils.ss

enum class Controls {
    Halt,
    MemRegI, MemI, MemO,
    CntE, CntO, CntI, CntCl,
    InstRegI, InstRegO,
    ARegI, ARegO,
    BRegI, BRegO,
    Subst, AluO,
    OutRegI,
    Unused
}

fun initControls() = Controls.values()
    .map { it to sig(1) }
    .toMap()

fun initCounter(clk: Signals, controls: Map<Controls, Signals>, bus: Signals) {
    val clear = controls[CntCl]!!
    val ci = controls[CntI]!!
    val ce = controls[CntE]!!
    val counterIn = clear + ci + ce + clk + bus.ss(0..3)
    val counterDirectOur = sig(4)
    syncCounterWithEnable(counterIn, counterDirectOur)
    LED(counterDirectOur, "COUNTER DIRECT OUT")
    controls[CntE]!!.forceUpdate(0)
    controls[CntO]!!.forceUpdate(0)
    controls[CntCl]!!.forceUpdate(0)
    connectToBus(controls[CntO]!!, bus.ss(0..3), counterDirectOur)
}

fun initControlUnit(clk: Signals, controls: Map<Controls, Signals>, instrRegOutput: Signals) {
    val controllerOut = controls[Halt]!! + controls[MemRegI]!! + controls[MemI]!! + controls[MemO]!! +
            controls[InstRegO]!! + controls[InstRegI]!! + controls[ARegI]!! + controls[ARegO]!! +
            controls[Unused]!! + controls[AluO]!! + controls[Subst]!! + controls[BRegI]!! +
            controls[OutRegI]!! + controls[CntE]!! + controls[CntO]!! + controls[CntCl]!!
    LED(controllerOut, "controller")
    controller(clk + instrRegOutput.ss(4..7), controllerOut)
}

fun initAlu(
    controls: Map<Controls, List<Signal>>,
    bus: List<Signal>,
    aRegDirectOut: List<Signal>,
    bRegDirectOut: List<Signal>
) {
    val aluDirectOut = sig(8)
    LED(aluDirectOut, "ALU")
    alu(controls[Subst]!! + aRegDirectOut + bRegDirectOut, aluDirectOut + sig(1))
    connectToBus(controls[AluO]!!, bus, aluDirectOut)
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

fun initMemory(controls: Map<Controls, Signals>, memReg: Signals, bus: Signals) {
    val memDirectOut = sig(8)
    val wr = controls[MemI]!!
    val rd = controls[MemO]!!
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
fun initInstrReg(clk: Signals, controls: Map<Controls, List<Signal>>, bus: List<Signal>): List<Signal> {
    val directOut = sig(8)
    val en = controls[InstRegI]!!
    register(clk + en + bus, directOut)
    connectToBus(controls[InstRegO]!!, bus, directOut)

    return directOut
}

fun initMemReg(clk: Signals, controls: Map<Controls, List<Signal>>, bus: List<Signal>): List<Signal> {
    val directOut = sig(8)
    val en = controls[MemRegI]!!
    register(clk + en + bus.ss(0..3)+sig(4), directOut)

    return directOut
}

fun initAReg(clk: Signals, controls: Map<Controls, List<Signal>>, bus: List<Signal>): List<Signal> {
    val directOut = sig(8)
    register(clk + controls[ARegI]!! + bus, directOut)
    connectToBus(controls[ARegO]!!, bus, directOut)
    return directOut
}

fun initBReg(clk: Signals, controls: Map<Controls, List<Signal>>, bus: List<Signal>): List<Signal> {
    val directOut = sig(8)
    register(clk + controls[BRegI]!! + bus, directOut)
    connectToBus(controls[BRegO]!!, bus, directOut)
    return directOut
}

fun initOutReg(clk: Signals, controls: Map<Controls, List<Signal>>, bus: List<Signal>): List<Signal> {
    val directOut = sig(8)
    register(clk + controls[OutRegI]!! + bus, directOut)
    return directOut
}

fun connectToBus(en: Signals, bus: Signals, out: Signals) {
    out.forEachIndexed { i, s ->
        TriStateGate(listOf(s) + en, bus.ss(i))
    }
}

fun initPeka() {
    val controls = initControls()
    val (clk, bus) = sigs(1, 8)
    val instRegDirectOut = initInstrReg(clk, controls, bus)
    val memRegDirectOut = initMemReg(clk, controls, bus)
    val aRegDirectOut = initAReg(clk, controls, bus)
    val bRegDirectOut = initBReg(clk, controls, bus)
    val outpRegDirectOut = initOutReg(clk, controls, bus)

    initCounter(clk, controls, bus)
    initMemory(controls, memRegDirectOut, bus)
    initAlu(controls, bus, aRegDirectOut, bRegDirectOut)
    initControlUnit(clk, controls, instRegDirectOut)

    LED(memRegDirectOut, "MEM REG")
    LED(outpRegDirectOut, "OUTPUT")
    LED(instRegDirectOut, "INST REG")
    LED(aRegDirectOut, "A REG")
    LED(bRegDirectOut, "B REG")
    LED(bus, "BUS")
    LED(clk, "CLOCK")

    clkReset!!.forceUpdate(1)
    controls[CntCl]!!.forceUpdate(1)

    clk.forceUpdate(1)
    clk.forceUpdate(0)

    clkReset!!.forceUpdate(0)
    controls[CntCl]!!.forceUpdate(0)
    controls[CntI]!!.forceUpdate(0)

    printLeds()
    println("start update")
    for (i in 0..12) {
        println("------- step $i")
        clk.forceUpdate(1)
        clk.forceUpdate(0)
        printLeds()
        println("------- step $i")
    }
}

fun printLeds() = leds.forEach { it.prnt() }
