package com.dzharvis

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class GatesKtTest {

    @Test
    fun `register test`() {
        val (clk, load, data, dataOut) = sigs(1, 1, 8, 8)
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
        val (clk, load, clear, enable, dataIn, dataOut) = sigs(1, 1, 1, 1, 4, 4)
        syncCounterWithEnable(clear + load + enable + clk + dataIn, dataOut)

        LED(dataOut)

        clear.forceUpdate(true)
        pushClk(clk)

        val q4Dbg = ST_DBG(dataOut.ss(3))
        val q3Dbg = ST_DBG(dataOut.ss(2))
        val q2Dbg = ST_DBG(dataOut.ss(1))
        val q1Dbg = ST_DBG(dataOut.ss(0))

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
        val (clk, cnt) = sigs(1, 4)
        counter(clk, cnt)

        val q4Dbg = ST_DBG(cnt.ss(3))
        val q3Dbg = ST_DBG(cnt.ss(2))
        val q2Dbg = ST_DBG(cnt.ss(1))
        val q1Dbg = ST_DBG(cnt.ss(0))

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
        val (j, k, q, nq, clk) = sigs(1, 1, 1, 1, 1)

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

    @Test
    fun `andn test`() {
        val (inp, outp) = sigs(4, 1)
        andn(inp, outp, 4)
        inp.forceUpdate(true, false, false, false)
        assertEquals(false, outp[0].signal)
        inp.forceUpdate(true, false, true, false)
        assertEquals(false, outp[0].signal)
        inp.forceUpdate(true, true, true, false)
        assertEquals(false, outp[0].signal)

        inp.forceUpdate(true, true, true, true)
        assertEquals(true, outp[0].signal)
    }

    @Test
    fun `decoder test`() {
        val (inp, outp) = sigs(5, 16)
        decoder(inp, outp, 4)
        inp.forceUpdate(true, false, false, false, false)
        assertEquals(
            listOf(
                true, false, false, false,
                false, false, false, false,
                false, false, false, false,
                false, false, false, false
            ),
            outp.asBools()
        )


        inp.forceUpdate(true, true, false, false, false)
        assertEquals(
            listOf(
                false, true, false, false,
                false, false, false, false,
                false, false, false, false,
                false, false, false, false
            ),
            outp.asBools()
        )

        inp.forceUpdate(true, false, false, false, true)
        assertEquals(
            listOf(
                false, false, false, false,
                false, false, false, false,
                true, false, false, false,
                false, false, false, false
            ),
            outp.asBools()
        )

        inp.forceUpdate(true, true, true, true, true)
        assertEquals(
            listOf(
                false, false, false, false,
                false, false, false, false,
                false, false, false, false,
                false, false, false, true
            ),
            outp.asBools()
        )

        inp.forceUpdate(false, true, true, true, true)
        assertEquals(
            listOf(
                false, false, false, false,
                false, false, false, false,
                false, false, false, false,
                false, false, false, false
            ),
            outp.asBools()
        )
    }

    private fun pushClk(clcIn: List<Signal>) {
        clcIn.forceUpdate(true)
        clcIn.forceUpdate(false)
        println("---")
    }

}
