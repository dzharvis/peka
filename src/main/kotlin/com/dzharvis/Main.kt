package com.dzharvis

fun main() {
    val index: SignalIndex = mutableMapOf()
    manualClock(index)
    val clcIn = index.extract("clcIn").subSignal(0)
    LED(index.extract("clcOut"))
    for(i in 0..10) {
        clcIn[0].forceUpdate(clcIn[0].signal.not())
    }
}