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
    var prevState = input[0].signal
    override fun mutateOutput(): Set<Gate> {
//        if (prevState != input[0].signal) {
//            prevState = input[0].signal
            println("$name: ${input.joinToString()}")
//        }
//        println("$name: ${input.joinToString()}")
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

fun fullAdder(input: Signals, output: Signals): Signals {
    val carry = input[2]
    val xorO1 = XOR(input, sigs(1)).output
    val andO1 = AND(input, sigs(1)).output
    val andO2 = AND(xorO1 + carry, sigs(1)).output
    val xorO2 = XOR(xorO1 + carry, output.subSignal(0)).output
    val orO1 = OR(andO2 + andO1, output.subSignal(1)).output
    return output
}

fun fullAdder4(input: Signals, output: Signals): Signals {
    val (s1, c1) = fullAdder(input.subSignal(0..2), output.subSignal(0) + sigs(1))
    val (s2, c2) = fullAdder(input.subSignal(3..4) + c1, output.subSignal(1) + sigs(1))
    val (s3, c3) = fullAdder(input.subSignal(5..6) + c2, output.subSignal(2) + sigs(1))
    val (s4, c4) = fullAdder(input.subSignal(7..8) + c3, output.subSignal(3..4))
    return output
}

fun pulser(input: Signals, output: Signals): Signals {
    val not = sigs(1)
    val and = AND(input + not, output).output
    // AND should be updated first to achieve short Pulse effect as in real circuit
    NOT(input, not)
    return and
}


fun infOscilator(input: Signals, output: Signals): Signals {
    require(input.subSignal(0) == output.subSignal(0))
    { "infinite oscilator requires output to be connected to input" }

    return NOT(input, output).output
}

// d - 0, en - 1
fun dLatch(input: Signals, output: Signals): Signals {
    val en = input.subSignal(1)
    val d = input.subSignal(0)
    val inv = NOT(d, sigs(1)).output
    val and1 = AND(inv + en, sigs(1)).output
    val and2 = AND(d + en, sigs(1)).output
    val nor1Out = output.subSignal(0)
    val nor2Out = output.subSignal(1)
    NOR(and1 + nor2Out, nor1Out).output
    NOR(and2 + nor1Out, nor2Out).output
    return output
}

fun msJKFlipFlop(input: Signals, output: Signals): Signals {
    val j = input.subSignal(0)
    val k = input.subSignal(1)
    val clk_ = NOT(input.subSignal(2), sigs(1)).output
    val clk = pulser(clk_, sigs(1))
    val nclk = NOT(clk, sigs(1)).output

    val q = output.subSignal(0)
    val nq = output.subSignal(1)
    val and3outp1 = NAND3(j + nq + clk, sigs(1)).output
    val and3outp2 = NAND3(k + q + clk, sigs(1)).output
    val r = output.subSignal(0)
    val s = output.subSignal(1)
    NAND(and3outp1 + s, r).output
    NAND(and3outp2 + r, s).output
    val a = NAND(r + nclk, sigs(1)).output
    val b = NAND(s + nclk, sigs(1)).output
    NAND(a + nq, q)
    NAND(b + q, nq)
    return output
}
