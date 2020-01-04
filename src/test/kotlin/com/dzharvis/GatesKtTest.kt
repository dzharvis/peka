package com.dzharvis

import org.junit.Test

import org.junit.jupiter.api.Assertions.*

class GatesKtTest {

    @Test
    fun counter() {
        val clk = sigs(1)
        val cnt = sigs(4)
        counter(clk, cnt)

        val q4Dbg = ST_DBG(cnt.subSignal(3))
        val q3Dbg = ST_DBG(cnt.subSignal(2))
        val q2Dbg = ST_DBG(cnt.subSignal(1))
        val q1Dbg = ST_DBG(cnt.subSignal(0))

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
        j[0].forceUpdate(true)
        k[0].forceUpdate(false)
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
        j[0].forceUpdate(false)
        k[0].forceUpdate(true)
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
        j[0].forceUpdate(true)
        k[0].forceUpdate(true)
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
        clcIn[0].forceUpdate(true)
        clcIn[0].forceUpdate(false)
    }

}
