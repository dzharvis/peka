package utils

import com.dzharvis.components.Signal
import com.dzharvis.components.Signals
import kotlin.math.pow

// because kotlin that's why
operator fun <T> List<T>.component6() = this[5]
operator fun <T> List<T>.component7() = this[6]
operator fun <T> List<T>.component8() = this[7]
operator fun <T> List<T>.component9() = this[8]
operator fun <T> List<T>.component10() = this[9]

fun Signals.bySize(vararg ranges: Int): List<Signals> {
    val ranges = ranges.fold<List<IntRange>>(listOf()) { acc, i ->
        if (acc.isEmpty()) {
            listOf(0 until i)
        } else {
            acc.last().last.let { end ->
                val nextRange = (end + 1) until (end + 1 + i)
                acc.plus(listOf(nextRange))
            }
        }
    }
    return ranges.map { this.ss(it) }
}

fun Signals.ss(range: IntRange) = this.slice(range)
fun Signals.ss(range: Int) = this.slice(range..range)
fun Signals.bits() = this.map { it.signal }

fun Int.sig() = Signal(this)
fun Int.powOfTwo() = 2.0.pow(this).toInt()

fun Int.invLastBit() = this.inv() and 1