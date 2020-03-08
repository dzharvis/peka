package com.dzharvis.components

import utils.invLastBit
import utils.ss

var counter = 0

abstract class Gate(input: Signals) {
    val id = ++counter
    override fun toString() = "$id"

    init {
//        graph.addNode(this.toString())
        this.dependsOn(input)
    }

    private fun dependsOn(signals: Signals) = signals.subscribe(this)
    abstract fun mutateOutput(): Set<Gate>
}

val leds = mutableListOf<LED>()

// for debugging
class LED(val input: Signals, val name: String = "LED") : Gate(input) {
    init {
        leds.add(this)
    }

    fun prnt() {
        println("$name:\n    ${input.joinToString("")}")
    }

    override fun mutateOutput(): Set<Gate> {
        // prnt()
        return emptySet()
    }
}

// for debugging
class TRACING_LED(val input: Signals, val name: String = "LED") : Gate(input) {
    fun prnt() {
        println("$name:\n    ${input.joinToString("")}")
    }

    override fun mutateOutput(): Set<Gate> {
        prnt()
        return emptySet()
    }
}

// for debugging
class ST_DBG(val input: Signals) : Gate(input) {
    val states = mutableListOf<Int>()
    override fun mutateOutput(): Set<Gate> {
        states.add(input[0].signal)
        return emptySet()
    }

    fun reset() = states.clear()
}

class NOT(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(if (input[0].signal == 1) 0 else 1)
}

class AND(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal and input[1].signal)
}

class AND3(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal and input[1].signal and input[2].signal)
}

class NAND3(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() =
        output[0].update((input[0].signal and input[1].signal and input[2].signal).invLastBit())
}

class NAND(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update((input[0].signal and input[1].signal).invLastBit())
}

class OR(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal or input[1].signal)
}

class OR3(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal or input[1].signal or input[2].signal)
}

class NOR(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update((input[0].signal or input[1].signal).invLastBit())
}

class XOR(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal xor input[1].signal)
}

// can physically disconnect element from circuit
class TriStateGate(val input: Signals, val output: Signals) : Gate(input) {
    private val inp = input.ss(0)[0]
    private val en = input.ss(1)[0]
    override fun mutateOutput(): Set<Gate> = if (en.signal == 1) {
        output[0].update(inp.signal)
    } else {
        emptySet()
    }
}
