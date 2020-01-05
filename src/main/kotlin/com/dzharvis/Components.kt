package com.dzharvis

// memory
// 2 registers, instruct register
// ALU
// clock
// controller
// counter


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
fun dFlipFlop(input: Signals, output: Signals): Signals {
    val d = input.subSignal(0)
    val clk = pulser(input.subSignal(1), sigs(1))
    val inv = NOT(d, sigs(1)).output
    val and1 = AND(inv + clk, sigs(1)).output
    val and2 = AND(d + clk, sigs(1)).output
    val nor1Out = output.subSignal(0)
    val nor2Out = output.subSignal(1)
    NOR(and1 + nor2Out, nor1Out)
    NOR(and2 + nor1Out, nor2Out)
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

fun manualClock(signals: SignalIndex) {
    val clc = signals.extract("clcIn")
    val clcOut = signals.extract("clcOut")
    pulser(clc, clcOut)
}

fun manualClock(input: Signals, output: Signals) {
    pulser(input, output)
}

fun clock(signals: SignalIndex) {
    TODO()
}

fun counter(input: Signals, output: Signals) {
    val clk = input

    val q1 = output.subSignal(0)
    val nq1 = sigs(1)
    msJKFlipFlop(nq1 + q1 + clk, q1 + nq1)

    val q2 = output.subSignal(1)
    val nq2 = sigs(1)
    msJKFlipFlop(nq2 + q2 + q1, q2 + nq2)

    val q3 = output.subSignal(2)
    val nq3 = sigs(1)
    msJKFlipFlop(nq3 + q3 + q2, q3 + nq3)

    val q4 = output.subSignal(3)
    val nq4 = sigs(1)
    msJKFlipFlop(nq4 + q4 + q3, q4 + nq4)
}

fun syncCounterWithEnable(input: Signals, output: Signals) {
    val clear = input.subSignal(0)
    val nclear = NOT(clear, sigs(1)).output
    val load = input.subSignal(1)
    val nload = NOT(load, sigs(1)).output
    val enable = input.subSignal(2)
    val clk = input.subSignal(3)

    val and1Out = AND(nclear + load, sigs(1)).output
    val and2Out = AND3(nclear + nload + enable, sigs(1)).output

    fun flipFlop(input: Signals, output: Signals) {
        val l = input.subSignal(0)
        val en = input.subSignal(1)
        val cl = input.subSignal(2)
        val data = input.subSignal(3)
        val nData = NOT(data, sigs(1)).output

        val and1 = AND(l + data, sigs(1)).output
        val and2 = AND(l + nData, sigs(1)).output

        val or1 = OR(en + and1, sigs(1)).output
        val or2 = OR3(cl + en + and2, sigs(1)).output

        val q = output.subSignal(0)
        val carry = output.subSignal(1)
        msJKFlipFlop(or1 + or2 + clk, q + sigs(1))

        AND(en + q, carry)
    }

    val carry1 = sigs(1)
    val carry2 = sigs(1)
    val carry3 = sigs(1)
    val carry4 = sigs(1)
    flipFlop(and1Out + and2Out + clear + input.subSignal(4), output.subSignal(0) + carry1)
    flipFlop(and1Out + carry1 + clear + input.subSignal(5), output.subSignal(1) + carry2)
    flipFlop(and1Out + carry2 + clear + input.subSignal(6), output.subSignal(2) + carry3)
    flipFlop(and1Out + carry3 + clear + input.subSignal(7), output.subSignal(3) + carry4)
}

fun register(input: Signals, output: Signals) {
    val clk = input.subSignal(0)
    val load = input.subSignal(1)
    val enable = AND(clk + load, sigs(1)).output
    val data = input.subSignal(2..9)

    for (i in 0..7) {
        dFlipFlop(data.subSignal(i) + enable, output.subSignal(i) + sigs(1))
    }
}

