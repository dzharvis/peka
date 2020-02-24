package com.dzharvis

fun main() {
    val (j, k, q, nq) = sigs(1, 1, 1, 1)

    val index: SignalIndex = mutableMapOf()
    manualClock(index)
    val clcIn = index.extract("clcIn").ss(0)
    val clk = index.extract("clcOut").ss(0)
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
