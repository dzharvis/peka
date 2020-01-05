package com.dzharvis


abstract class Gate(input: Signals) {
    init {
        this.dependsOn(input)
    }

    private fun dependsOn(signals: Signals) = signals.subscribe(this)
    abstract fun mutateOutput(): Set<Gate>
}

// for debugging
class LED(val input: Signals, val name: String = "LED") : Gate(input) {
    override fun mutateOutput(): Set<Gate> {
        println("$name: ${input.joinToString("")}")
        return emptySet()
    }
}

// for debugging
class ST_DBG(val input: Signals) : Gate(input) {
    val states = mutableListOf<Boolean>()
    override fun mutateOutput(): Set<Gate> {
        states.add(input[0].signal)
        return emptySet()
    }

    fun reset() = states.clear()
}

class NOT(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(!input[0].signal)
}

class AND(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal and input[1].signal)
}

class AND3(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal and input[1].signal and input[2].signal)
}

class NAND3(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update((input[0].signal and input[1].signal and input[2].signal).not())
}

class NAND(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update((input[0].signal and input[1].signal).not())
}

class OR(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal or input[1].signal)
}

class OR3(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal or input[1].signal or input[2].signal)
}

class NOR(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update((input[0].signal or input[1].signal).not())
}

class XOR(val input: Signals, val output: Signals) : Gate(input) {
    override fun mutateOutput() = output[0].update(input[0].signal xor input[1].signal)
}

class TriStateGate(val input: Signals, val output: Signals) : Gate(input) {
    private val inp = input.subSignal(0)[0]
    private val en = input.subSignal(1)[0]
    override fun mutateOutput(): Set<Gate> {
        return if (en.signal) {
            output[0].update(inp.signal)
        } else {
            emptySet()
        }
    }
}
