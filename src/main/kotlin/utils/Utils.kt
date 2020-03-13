package utils

import com.dzharvis.components.Signals
import com.dzharvis.components.forceUpdate

fun unzip(elems: List<Int>): List<List<Int>> {
    return if(elems.contains(-1)) {
        val i = elems.indexOf(-1)
        val newElems1 = elems.slice(0 until i) + 0 + elems.slice((i + 1) until elems.size)
        val newElems2 = elems.slice(0 until i) + 1 + elems.slice((i + 1) until elems.size)
        unzip(newElems1) + unzip(newElems2)
    } else {
        listOf(elems)
    }
}

fun initializeMemory(input: Signals, inpTable: List<List<Int>>, outTable: List<List<Int>>) {
    require(inpTable.size == outTable.size)
    // -1 means for any input value - which means that we should iterate over each possible value combinations
    val extractedTable = inpTable.zip(outTable).flatMap { (i, o) ->
        unzip(i).map {
            it to o
        }
    }
    //

    val (wr, rd, address, input) = input.bySize(1, 1, inpTable.first().size, 8)

    extractedTable.forEach { (currentAddress, currentValue) ->
        input.forceUpdate(*currentValue.toIntArray())
        require(input.bits() == currentValue)
        address.forceUpdate(*currentAddress.toIntArray())
        wr.forceUpdate(1)
        wr.forceUpdate(0)

        // check we car read value currently written
        input.forceUpdate(0, 0, 0, 0, 0, 0, 0, 0)
        address.forceUpdate(*generateSequence { 0 }.take(inpTable[0].size).toList().toIntArray())
        rd.forceUpdate(1)
        address.forceUpdate(*currentAddress.toIntArray())
        require(input.bits() == currentValue) {
            println("Expected ${currentValue}, got ${input.bits()}")
        }
        rd.forceUpdate(0)
    }
}

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

fun binToDec(data: List<Int>): Int {
    var result = 0
    for(bit in data.reversed()) {
        result = (result or bit) shl 1
    }
    return result shr 1
}

fun decToBin(data: Int, numBits: Int): List<Int> {
    var currentData = data
    return (0 until numBits).map {
        val bit = currentData and 1
        currentData = currentData shr 1
        bit
    }
}
