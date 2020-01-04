package com.dzharvis

// memory
// 2 registers, instruct register
// ALU
// clock
// controller
// counter

fun manualClock(signals: SignalIndex) {
    val clc = signals.extract("clcIn")
    val clcOut = signals.extract("clcOut")
    pulser(clc, clcOut)
}

fun manualClock(input: Signals, output: Signals) {
    pulser(input, output)
}

fun clock(signals: SignalIndex) {
    TODO()
}

fun counter(input: Signals, output: Signals) {
    val clk = input

    val q1 = output.subSignal(0)
    val nq1 = sigs(1)
    msJKFlipFlop(nq1 + q1 + clk, q1 + nq1)

    val q2 = output.subSignal(1)
    val nq2 = sigs(1)
    msJKFlipFlop(nq2 + q2 + q1, q2 + nq2)

    val q3 = output.subSignal(2)
    val nq3 = sigs(1)
    msJKFlipFlop(nq3 + q3 + q2, q3 + nq3)

    val q4 = output.subSignal(3)
    val nq4 = sigs(1)
    msJKFlipFlop(nq4 + q4 + q3, q4 + nq4)

}

