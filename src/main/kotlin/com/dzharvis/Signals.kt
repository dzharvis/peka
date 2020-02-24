package com.dzharvis

import java.util.*

fun sig(size: Int): Signals = generateSequence { false.sig() }.take(size).toList()
fun sigs(vararg size: Int): List<Signals> = size.map { generateSequence { false.sig() }.take(it).toList() }

class Signal(var signal: Boolean) {
    private val dependentGates = mutableSetOf<Gate>()
    private var isUninitialized: Boolean = true
    // trampoline magic to avoid stack overflow in complex schemes
    private fun notifySignalChanged() {
        val dependencies = LinkedList<Gate>()
        dependencies.addAll(dependentGates)
        while (dependencies.isNotEmpty()) {
            val first = dependencies.first()
            dependencies.remove(first)
            val newDeps = first.mutateOutput()
            dependencies.addAll(newDeps)
        }
    }

    fun forceUpdate(newSignal: Boolean) {
        update(newSignal)
        notifySignalChanged()
    }

    fun subscribe(gate: Gate) = dependentGates.add(gate)
    override fun toString() = if (signal) "[x]" else "[ ]"
    fun update(newValue: Boolean): Set<Gate> {
        return if (signal != newValue || isUninitialized) {
            isUninitialized = false
            signal = newValue
            dependentGates
        } else {
            emptySet()
        }
    }
}

class Destructable(
    val signals: Signals,
    val ranges: List<IntRange>
) {
    operator fun component1() = signals.ss(ranges[0])
    operator fun component2() = signals.ss(ranges[1])
    operator fun component3() = signals.ss(ranges[2])
    operator fun component4() = signals.ss(ranges[3])
    operator fun component5() = signals.ss(ranges[4])
    operator fun component6() = signals.ss(ranges[5])
    operator fun component7() = signals.ss(ranges[6])
    operator fun component8() = signals.ss(ranges[7])
    operator fun component9() = signals.ss(ranges[8])
    operator fun component10() = signals.ss(ranges[9])
}

operator fun List<Signals>.component6() = this[5]

typealias Signals = List<Signal>

fun Signals.ss(range: IntRange) = this.slice(range)
fun Signals.ss(range: Int) = this.slice(range..range)
fun Signals.destr(vararg ranges: Any) = Destructable(this, ranges.map {
    when (it) {
        is Int -> IntRange(it, it)
        is IntRange -> it
        else -> throw IllegalArgumentException("oops")
    }
})

fun Signals.subscribe(gate: Gate) = this.forEach { it.subscribe(gate) }
fun Signals.forceUpdate(vararg value: Boolean) = value.forEachIndexed { i, s -> this[i].forceUpdate(s) }
fun List<Boolean>.asSig() = this.map { it.sig() }
fun Boolean.sig() = Signal(this)

typealias SignalIndex = MutableMap<String, Signal>

fun SignalIndex.extract(vararg names: String): Signals = names.map {
    this.putIfAbsent(it, Signal(false))
    this[it]!!
}

