package utils

import com.dzharvis.Signal
import com.dzharvis.Signals

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

fun Signals.bySize(vararg ranges: Int): Destructable {
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
    return Destructable(this, ranges)
}

fun Signals.ss(range: IntRange) = this.slice(range)
fun Signals.ss(range: Int) = this.slice(range..range)
fun Signals.bits() = this.map { it.signal }

fun Int.sig() = Signal(this)
