package com.dzharvis

fun flipper(): () -> List<Boolean> {
    var state = true
    return {
        state = !state
        listOf(state)
    }
}

fun divideByTwo(otherDivider: () -> List<Boolean>): () -> List<Boolean> {
    var state = false
    var lastRes = false
    return {
        val currRes = otherDivider()
        if (!currRes.last() && currRes.last() != lastRes) {
            state = !state
            lastRes = false
        }
        lastRes = currRes.last()
        currRes + state
    }
}

fun nBitBinaryCounterSim(n: Int): () -> List<Boolean> {
    return if (n == 1) flipper()
    else divideByTwo(nBitBinaryCounterSim(n - 1))
}