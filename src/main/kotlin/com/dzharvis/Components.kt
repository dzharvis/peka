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

fun clock(signals: SignalIndex) {
    TODO()
}