package com.dzharvis

fun main() {
    val j = sigs(1)
    val k = sigs(1)
    val q = sigs(1)
    val nq = sigs(1)

    val index: SignalIndex = mutableMapOf()
    manualClock(index)
    val clcIn = index.extract("clcIn").subSignal(0)
    val clk = index.extract("clcOut").subSignal(0)
//    LED(clk, "clock")
    LED(q, "Q")
//    LED(nq, "nQ")

    msJKFlipFlop(j + k + clk, q + nq)

    j[0].forceUpdate(true)
    k[0].forceUpdate(false)
    for (i in 0..2) {
        clcIn[0].forceUpdate(clcIn[0].signal.not())
        println("________")
    }
}
