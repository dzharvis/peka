package com.dzharvis

import utils.Destructable
import utils.sig
import java.util.*

fun sig(size: Int): Signals = generateSequence { 0.sig() }.take(size).toList()
fun sigs(vararg size: Int): List<Signals> = size.map { generateSequence { 0.sig() }.take(it).toList() }

class Signal(var signal: Int) {
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

    fun forceUpdate(newSignal: Int) {
        update(newSignal)
        notifySignalChanged()
    }

    fun subscribe(gate: Gate) = dependentGates.add(gate)
    override fun toString() = if (signal == 1) "[x]" else "[ ]"
    fun update(newValue: Int): Set<Gate> {
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




fun Signals.subscribe(gate: Gate) = this.forEach { it.subscribe(gate) }
fun Signals.forceUpdate(vararg value: Int) = value.forEachIndexed { i, s -> this[i].forceUpdate(s) }
//fun Signals.asBools() = this.map { it.signal }

//typealias SignalIndex = MutableMap<String, Signal>
//
//fun SignalIndex.extract(vararg names: String): Signals = names.map {
//    this.putIfAbsent(it, Signal(0))
//    this[it]!!
//}

