package com.dzharvis

import org.junit.Test

import org.junit.jupiter.api.Assertions.*

class GatesKtTest {

    @Test
    fun `register test`() {
        val clk = sigs(1)
        val load = sigs(1)
        val data = sigs(8)
        val dataOut = sigs(8)
        register(clk + load + data, dataOut)
        LED(dataOut)

        pushClk(clk)
        data.forceUpdate(false, false, false, false, false, false, false, false)
        load.forceUpdate(true)
        pushClk(clk)
        load.forceUpdate(false)
        pushClk(clk)

        assertEquals(listOf(false, false, false, false, false, false, false, false), dataOut.map { it.signal })

        pushClk(clk)
        pushClk(clk)

        data.forceUpdate(false, false, true, false, false, false, true, false)
        load.forceUpdate(true)
        pushClk(clk)
        load.forceUpdate(false)
        pushClk(clk)
        assertEquals(listOf(false, false, true, false, false, false, true, false), dataOut.map { it.signal })
    }

    @Test
    fun `sync counter with enable`() {
        val clk = sigs(1)
        val load = sigs(1)
        val clear = sigs(1)
        val enable = sigs(1)
        val dataIn = sigs(4)
        val dataOut = sigs(4)
        syncCounterWithEnable(clear + load + enable + clk + dataIn, dataOut)

        LED(dataOut)

        clear.forceUpdate(true)
        pushClk(clk)

        val q4Dbg = ST_DBG(dataOut.subSignal(3))
        val q3Dbg = ST_DBG(dataOut.subSignal(2))
        val q2Dbg = ST_DBG(dataOut.subSignal(1))
        val q1Dbg = ST_DBG(dataOut.subSignal(0))

        enable.forceUpdate(true)
        load.forceUpdate(false)
        clear.forceUpdate(false)
        for (i in 1..16) {
            pushClk(clk)
        }

        clear.forceUpdate(true)
        pushClk(clk)

        assertEquals(listOf(true, false), q4Dbg.states)
        assertEquals(listOf(true, false, true, false), q3Dbg.states)
        assertEquals(listOf(true, false, true, false, true, false, true, false), q2Dbg.states)
        assertEquals(
            listOf(
                true, false, true, false, true, false, true, false,
                true, false, true, false, true, false, true, false
            ), q1Dbg.states
        )

        q1Dbg.reset()
        q2Dbg.reset()
        q3Dbg.reset()
        q4Dbg.reset()

        dataIn.forceUpdate(true, false, true, false)

        enable.forceUpdate(false)
        clear.forceUpdate(false)
        load.forceUpdate(true)
        pushClk(clk)

        assertEquals(listOf(true, false, true, false), dataOut.map { it.signal })


        enable.forceUpdate(true)
        clear.forceUpdate(false)
        load.forceUpdate(false)
        pushClk(clk)
        pushClk(clk)
        pushClk(clk)

        assertEquals(listOf(false, false, false, true), dataOut.map { it.signal })

        clear.forceUpdate(true)
        pushClk(clk)

        assertEquals(listOf(false, false, false, false), dataOut.map { it.signal })

        assertEquals(listOf(true, false), q4Dbg.states)
        assertEquals(listOf(true, false), q3Dbg.states)
        assertEquals(listOf(true, false), q2Dbg.states)
        assertEquals(listOf(true, false, true, false), q1Dbg.states)
    }

    @Test
    fun counter() {
        val clk = sigs(1)
        val cnt = sigs(4)
        counter(clk, cnt)

        val q4Dbg = ST_DBG(cnt.subSignal(3))
        val q3Dbg = ST_DBG(cnt.subSignal(2))
        val q2Dbg = ST_DBG(cnt.subSignal(1))
        val q1Dbg = ST_DBG(cnt.subSignal(0))

        LED(cnt)
        pushClk(clk)
        assertEquals(listOf(false, false, false, false), cnt.map { it.signal }.reversed())

        pushClk(clk)
        assertEquals(listOf(false, false, false, true), cnt.map { it.signal }.reversed())

        pushClk(clk)
        assertEquals(listOf(false, false, true, false), cnt.map { it.signal }.reversed())

        pushClk(clk)
        assertEquals(listOf(false, false, true, true), cnt.map { it.signal }.reversed())

        pushClk(clk)
        assertEquals(listOf(false, true, false, false), cnt.map { it.signal }.reversed())

        pushClk(clk)
        assertEquals(listOf(false, true, false, true), cnt.map { it.signal }.reversed())

        pushClk(clk)
        assertEquals(listOf(false, true, true, false), cnt.map { it.signal }.reversed())

        pushClk(clk)
        assertEquals(listOf(false, true, true, true), cnt.map { it.signal }.reversed())

        pushClk(clk)
        assertEquals(listOf(true, false, false, false), cnt.map { it.signal }.reversed())

        for (i in 9..15) {
            pushClk(clk)
        }
        assertEquals(listOf(true, true, true, true), cnt.map { it.signal }.reversed())
        assertEquals(listOf(true, false, true), q4Dbg.states)
        assertEquals(listOf(true, false, true, false, true), q3Dbg.states)
        assertEquals(listOf(true, false, true, false, true, false, true, false, true), q2Dbg.states)
        assertEquals(
            listOf(
                true, false, true, false, true, false, true, false, true,
                false, true, false, true, false, true, false, true
            ),
            q1Dbg.states
        )
    }

    @Test
    fun `master-slave JK FlipFlop`() {
        val j = sigs(1)
        val k = sigs(1)
        val q = sigs(1)
        val nq = sigs(1)

        val clk = sigs(1)

        msJKFlipFlop(j + k + clk, q + nq)
        val qDbg = ST_DBG(q)
        val nqDbg = ST_DBG(nq)
        //</init>-------------------------------------
        // test state latch
        j.forceUpdate(true)
        k.forceUpdate(false)
        pushClk(clk)
        assertEquals(true, q[0].signal)
        qDbg.reset()
        nqDbg.reset()

        pushClk(clk)
        assertEquals(true, q[0].signal)
        assertEquals(listOf<Boolean>(), qDbg.states)
        assertEquals(listOf<Boolean>(), nqDbg.states)

        pushClk(clk)
        pushClk(clk)
        assertEquals(true, q[0].signal)
        assertEquals(listOf<Boolean>(), qDbg.states)
        assertEquals(listOf<Boolean>(), nqDbg.states)
        //--------------------------------------
        // test state latch
        j.forceUpdate(false)
        k.forceUpdate(true)
        pushClk(clk)
        assertEquals(listOf(false), qDbg.states)
        assertEquals(listOf(true), nqDbg.states)

        pushClk(clk)
        assertEquals(listOf(false), qDbg.states)
        assertEquals(listOf(true), nqDbg.states)

        pushClk(clk)
        pushClk(clk)
        assertEquals(listOf(false), qDbg.states)
        assertEquals(listOf(true), nqDbg.states)
        //---------------------------------------
        // test state switching
        j.forceUpdate(true)
        k.forceUpdate(true)
        pushClk(clk)
        assertEquals(listOf(false, true), qDbg.states)
        assertEquals(listOf(true, false), nqDbg.states)

        pushClk(clk)
        pushClk(clk)
        pushClk(clk)
        assertEquals(listOf(false, true, false, true, false), qDbg.states)
        assertEquals(listOf(true, false, true, false, true), nqDbg.states)
        //---------------------------------------
    }

    private fun pushClk(clcIn: List<Signal>) {
        clcIn.forceUpdate(true)
        clcIn.forceUpdate(false)
        println("---")
    }

}
