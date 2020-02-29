package com.dzharvis

import utils.bySize
import utils.ss
import kotlin.math.pow

// memory
// 2 registers, instruct register
// ALU
// clock
// controller
// counter


fun fullAdder(input: Signals, output: Signals): Signals {
    val carry = input[2]
    val xorO1 = XOR(input, sig(1)).output
    val andO1 = AND(input, sig(1)).output
    val andO2 = AND(xorO1 + carry, sig(1)).output
    val xorO2 = XOR(xorO1 + carry, output.ss(0)).output
    val orO1 = OR(andO2 + andO1, output.ss(1)).output
    return output
}

fun fullAdder4(input: Signals, output: Signals): Signals {
    val (s1, c1) = fullAdder(input.ss(0..2), output.ss(0) + sig(1))
    val (s2, c2) = fullAdder(input.ss(3..4) + c1, output.ss(1) + sig(1))
    val (s3, c3) = fullAdder(input.ss(5..6) + c2, output.ss(2) + sig(1))
    val (s4, c4) = fullAdder(input.ss(7..8) + c3, output.ss(3..4))
    return output
}

fun pulser(input: Signals, output: Signals): Signals {
    val not = sig(1)
    val and = AND(input + not, output).output
    // AND should be updated first to achieve short Pulse effect as in real circuit
    NOT(input, not)
    return and
}


fun infOscilator(input: Signals, output: Signals): Signals {
    require(input.ss(0) == output.ss(0))
    { "infinite oscilator requires output to be connected to input" }

    return NOT(input, output).output
}

// d - 0, en - 1
fun dFlipFlop(input: Signals, output: Signals): Signals {
    val d = input.ss(0)
    val clk = pulser(input.ss(1), sig(1))
    val inv = NOT(d, sig(1)).output
    val and1 = AND(inv + clk, sig(1)).output
    val and2 = AND(d + clk, sig(1)).output
    val nor1Out = output.ss(0)
    val nor2Out = output.ss(1)
    NOR(and1 + nor2Out, nor1Out)
    NOR(and2 + nor1Out, nor2Out)
    return output
}

fun msJKFlipFlop(input: Signals, output: Signals): Signals {
    val (j, k) = input.bySize(1, 1)
    val clk_ = NOT(input.ss(2), sig(1)).output
    val clk = pulser(clk_, sig(1))
    val nclk = NOT(clk, sig(1)).output

    val q = output.ss(0)
    val nq = output.ss(1)
    val and3outp1 = NAND3(j + nq + clk, sig(1)).output
    val and3outp2 = NAND3(k + q + clk, sig(1)).output
    val r = output.ss(0)
    val s = output.ss(1)
    NAND(and3outp1 + s, r).output
    NAND(and3outp2 + r, s).output
    val a = NAND(r + nclk, sig(1)).output
    val b = NAND(s + nclk, sig(1)).output
    NAND(a + nq, q)
    NAND(b + q, nq)
    return output
}

//fun manualClock(signals: SignalIndex) {
//    val clc = signals.extract("clcIn")
//    val clcOut = signals.extract("clcOut")
//    pulser(clc, clcOut)
//}

fun manualClock(input: Signals, output: Signals) {
    pulser(input, output)
}

//fun clock(signals: SignalIndex) {
//    TODO()
//}

fun counter(input: Signals, output: Signals) {
    val clk = input

    val q1 = output.ss(0)
    val nq1 = sig(1)
    msJKFlipFlop(nq1 + q1 + clk, q1 + nq1)

    val q2 = output.ss(1)
    val nq2 = sig(1)
    msJKFlipFlop(nq2 + q2 + q1, q2 + nq2)

    val q3 = output.ss(2)
    val nq3 = sig(1)
    msJKFlipFlop(nq3 + q3 + q2, q3 + nq3)

    val q4 = output.ss(3)
    val nq4 = sig(1)
    msJKFlipFlop(nq4 + q4 + q3, q4 + nq4)
}

fun syncCounterWithEnable(input: Signals, output: Signals) {
    val (clear, load, enable, clk) = input.bySize(1, 1, 1, 1)
    val nclear = NOT(clear, sig(1)).output
    val nload = NOT(load, sig(1)).output

    val and1Out = AND(nclear + load, sig(1)).output
    val and2Out = AND3(nclear + nload + enable, sig(1)).output

    fun flipFlop(input: Signals, output: Signals) {
        val (l, en, cl, data) = input.bySize(1,1,1,1)
        val nData = NOT(data, sig(1)).output

        val and1 = AND(l + data, sig(1)).output
        val and2 = AND(l + nData, sig(1)).output

        val or1 = OR(en + and1, sig(1)).output
        val or2 = OR3(cl + en + and2, sig(1)).output

        val q = output.ss(0)
        val carry = output.ss(1)
        msJKFlipFlop(or1 + or2 + clk, q + sig(1))

        AND(en + q, carry)
    }

    val (carry1, carry2, carry3, carry4) = sigs(1, 1, 1, 1)
    flipFlop(and1Out + and2Out + clear + input.ss(4), output.ss(0) + carry1)
    flipFlop(and1Out + carry1 + clear + input.ss(5), output.ss(1) + carry2)
    flipFlop(and1Out + carry2 + clear + input.ss(6), output.ss(2) + carry3)
    flipFlop(and1Out + carry3 + clear + input.ss(7), output.ss(3) + carry4)
}

fun andn(input: Signals, output: Signals, size: Int) {
    var carry = input.ss(0)
    for (i in 1 until (size - 1)) {
        carry = AND(carry + input.ss(i), sig(1)).output
    }
    AND(carry + input.ss(size - 1), output)
}

fun register(input: Signals, output: Signals) {
    val (clk, load, data) = input.bySize(1, 1, 8)
    val enable = AND(clk + load, sig(1)).output

    for (i in 0..7) {
        dFlipFlop(data.ss(i) + enable, output.ss(i) + sig(1))
    }
}

fun register8BitTriState(input: Signals, output: Signals) {
    val (wr, rd, input) = input.bySize(1, 1, 8)
    for (i in 0..7) {
        val trstIn = sig(1)
        TriStateGate(trstIn + rd, output.ss(i))
        dFlipFlop(input.ss(i) + wr, trstIn + sig(1))
    }
}

fun decoder(input: Signals, output: Signals, size: Int) {
    val (en, inp) = input.bySize(1, size)
    val notInp = (0 until size).map { i ->
        NOT(inp.ss(i), sig(1)).output[0]
    }

    val numOutputs: Int = size.powOfTwo()
    val counter = nBitBinaryCounterSim(size)

    for (i in 0 until numOutputs) {
        val pinsState = counter()
        val andInputs = pinsState
            .mapIndexed { i, st -> if (st == 1) inp.ss(i) else notInp.ss(i) }
            .flatten()
        andn(andInputs + en, output.ss(i), size + 1)
    }
}

// input and output is the same here as this is a bus
fun memory8Bit(input: Signals, output: Signals, size: Int) {
    val (wr, rd, addr, input) = input.bySize(1, 1, size, 8)
    val numCells = size.powOfTwo()

    val writeDecoded = sig(numCells)
    decoder(wr + addr, writeDecoded, size)

    val readDecoded = sig(numCells)
    decoder(rd + addr, readDecoded, size)

    for (i in 0 until numCells) {
        register8BitTriState(writeDecoded.ss(i) + readDecoded.ss(i) + input, input)
    }
}

fun Int.powOfTwo() = this.toDouble().pow(2.0).toInt()
