package com.dzharvis

import java.util.*

fun sigs(size: Int): Signals = generateSequence { false.sig() }.take(size).toList()

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

typealias Signals = List<Signal>

fun Signals.subSignal(range: IntRange) = this.slice(range)
fun Signals.subSignal(range: Int) = this.slice(range..range)
fun Signals.subscribe(gate: Gate) = this.forEach { it.subscribe(gate) }
fun Signals.forceUpdate(vararg value: Boolean) = value.forEachIndexed { i, s -> this[i].forceUpdate(s) }
fun List<Boolean>.asSig() = this.map { it.sig() }
fun Boolean.sig() = Signal(this)

typealias SignalIndex = MutableMap<String, Signal>

fun SignalIndex.extract(vararg names: String): Signals = names.map {
    this.putIfAbsent(it, Signal(false))
    this[it]!!
}
