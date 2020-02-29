package com.dzharvis

fun flipper(): () -> List<Int> {
    var state = 1
    return {
        state = state.invLastBit()
        listOf(state)
    }
}

fun divideByTwo(otherDivider: () -> List<Int>): () -> List<Int> {
    var state = 0
    var lastRes = 0
    return {
        val currRes = otherDivider()
        if (currRes.last() == 0 && currRes.last() != lastRes) {
            state = state.invLastBit()
            lastRes = 0
        }
        lastRes = currRes.last()
        currRes + state
    }
}

fun nBitBinaryCounterSim(n: Int): () -> List<Int> {
    return if (n == 1) flipper()
    else divideByTwo(nBitBinaryCounterSim(n - 1))
}