package com.dzharvis.peka

import com.dzharvis.components.*
import com.dzharvis.peka.Controls.*
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
    val counterDirectOur = sig(8)
    syncCounterWithEnable(counterIn, counterDirectOur)
    LED(counterDirectOur, "COUNTER DIRECT OUT")
    connectToBus(controls[CntO]!!, bus, counterDirectOur)


    bus.ss(0..3).forceUpdate(0, 0, 0, 0)
    ci.forceUpdate(1)
    ce.forceUpdate(0)
    clear.forceUpdate(0)
    clk.forceUpdate(1)
    clk.forceUpdate(0)
    clk.forceUpdate(1)
    ci.forceUpdate(0)
    ce.forceUpdate(1) // always enable


}

fun initControlUnit(clk: Signals, controls: Map<Controls, Signals>, instrRegOutput: Signals) {
    val controllerOut = controls[Halt]!! + controls[MemRegI]!! + controls[MemI]!! + controls[MemO]!! +
            controls[InstRegO]!! + controls[InstRegI]!! + controls[ARegI]!! + controls[ARegO]!! +
            controls[Unused]!! + controls[AluO]!! + controls[Subst]!! + controls[BRegI]!! +
            controls[OutRegI]!! + controls[CntE]!! + controls[CntO]!! + controls[CntCl]!!
    LED(controllerOut, "controller")
    controller(clk + instrRegOutput.ss(0..3), controllerOut)
}
fun initAlu(
    controls: Map<Controls, List<Signal>>,
    bus: List<Signal>,
    aRegDirectOut: List<Signal>,
    bRegDirectOut: List<Signal>
) {
    val aluDirectOut = sig(8)
    alu(controls[Subst]!! + aRegDirectOut + bRegDirectOut, aluDirectOut + sig(1))
    connectToBus(controls[AluO]!!, bus, aluDirectOut)
}
fun initMemory(controls: Map<Controls, Signals>, memReg: Signals, bus: Signals) {
    val memDirectOut = sig(8)
    memory8Bit(controls[MemI]!! + controls[MemO]!! + memReg + bus, memDirectOut, 8)
    connectToBus(controls[MemO]!!, bus, memDirectOut)
}
// first half - instruction code, second half - data or addr
fun initInstrReg(clk: Signals, controls: Map<Controls, List<Signal>>, bus: List<Signal>): List<Signal> {
    val directOut = sig(8)
    LED(directOut, "instr reg direct out")
    register(clk + controls[InstRegI]!! + bus, directOut)
    connectToBus(controls[InstRegO]!!, bus, directOut.ss(4..7) + sig(4))
    bus.forceUpdate(0, 0, 0, 0, 0, 0, 0, 0)
    controls[InstRegI]!!.apply {
        forceUpdate(1)
        clk.forceUpdate(0)
        clk.forceUpdate(1)
        forceUpdate(0)
    }
    return directOut
}
fun initMemReg(clk: Signals, controls: Map<Controls, List<Signal>>, bus: List<Signal>): List<Signal> {
    val directOut = sig(8)
    register(clk + controls[MemRegI]!! + bus, directOut)
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
    out.forEach { s ->
        TriStateGate(listOf(s) + en, bus)
    }
}

fun reset(data: Signals) {
    data.forEach { it.forceUpdate(0) }
}
fun initPeka() {
    val controls = initControls()
    val (clk, bus) = sigs(1, 8, 8)
    val instRegDirectOut = initInstrReg(clk, controls, bus)
    val memRegDirectOut = initMemReg(clk, controls, bus)
    val aRegDirectOut = initAReg(clk, controls, bus)
    val bRegDirectOut = initBReg(clk, controls, bus)
    val outpRegDirectOut = initOutReg(clk, controls, bus)

    initCounter(clk, controls, bus)
    initControlUnit(clk, controls, instRegDirectOut)
    initMemory(controls, memRegDirectOut, bus)
    initAlu(controls, bus, aRegDirectOut, bRegDirectOut)
    LED(outpRegDirectOut, "out")
    LED(bus, "bus")
    reset(instRegDirectOut)
    reset(memRegDirectOut)
    reset(aRegDirectOut)
    reset(bRegDirectOut)
    reset(outpRegDirectOut)
    reset(bus)

    println("start update")
    for(i in 0 .. 1000) {
        clk.forceUpdate(0)
        clk.forceUpdate(1)
        println("------------------------------------------------")
    }
}

