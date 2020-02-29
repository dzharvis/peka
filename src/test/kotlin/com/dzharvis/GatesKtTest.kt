package com.dzharvis

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import utils.component6
import utils.ss
import utils.bits

class GatesKtTest {

    @Test
    fun `register tri state test`() {
        val (wr, rd, data, dataOut) = sigs(1, 1, 8, 8)
        register8BitTriState(wr + rd + data, dataOut)
        LED(dataOut)

        data.forceUpdate(0, 0, 0, 0, 0, 0, 0, 0)
        wr.forceUpdate(1)
        wr.forceUpdate(0)

        assertEquals(listOf(0, 0, 0, 0, 0, 0, 0, 0), dataOut.bits())
        rd.forceUpdate(1)
        assertEquals(listOf(0, 0, 0, 0, 0, 0, 0, 0), dataOut.bits())
        rd.forceUpdate(0)


        data.forceUpdate(0, 0, 1, 0, 0, 0, 1, 0)
        wr.forceUpdate(1)
        wr.forceUpdate(0)
        rd.forceUpdate(1)
        assertEquals(listOf(0, 0, 1, 0, 0, 0, 1, 0), dataOut.bits())
        rd.forceUpdate(0)
        assertEquals(listOf(0, 0, 1, 0, 0, 0, 1, 0), dataOut.bits())
    }

    @Test
    fun `register test`() {
        val (clk, load, data, dataOut) = sigs(1, 1, 8, 8)
        register(clk + load + data, dataOut)
        LED(dataOut)

        pushClk(clk)
        data.forceUpdate(0, 0, 0, 0, 0, 0, 0, 0)
        load.forceUpdate(1)
        pushClk(clk)
        load.forceUpdate(0)
        pushClk(clk)

        assertEquals(listOf(0, 0, 0, 0, 0, 0, 0, 0), dataOut.bits())

        pushClk(clk)
        pushClk(clk)

        data.forceUpdate(0, 0, 1, 0, 0, 0, 1, 0)
        load.forceUpdate(1)
        pushClk(clk)
        load.forceUpdate(0)
        pushClk(clk)
        assertEquals(listOf(0, 0, 1, 0, 0, 0, 1, 0), dataOut.bits())
    }

    @Test
    fun `sync counter with enable`() {
        val (clk, load, clear, enable, dataIn, dataOut) = sigs(1, 1, 1, 1, 4, 4)
        syncCounterWithEnable(clear + load + enable + clk + dataIn, dataOut)

        LED(dataOut)

        clear.forceUpdate(1)
        pushClk(clk)

        val q4Dbg = ST_DBG(dataOut.ss(3))
        val q3Dbg = ST_DBG(dataOut.ss(2))
        val q2Dbg = ST_DBG(dataOut.ss(1))
        val q1Dbg = ST_DBG(dataOut.ss(0))

        enable.forceUpdate(1)
        load.forceUpdate(0)
        clear.forceUpdate(0)
        for (i in 1..16) {
            pushClk(clk)
        }

        clear.forceUpdate(1)
        pushClk(clk)

        assertEquals(listOf(1, 0), q4Dbg.states)
        assertEquals(listOf(1, 0, 1, 0), q3Dbg.states)
        assertEquals(listOf(1, 0, 1, 0, 1, 0, 1, 0), q2Dbg.states)
        assertEquals(
            listOf(
                1, 0, 1, 0, 1, 0, 1, 0,
                1, 0, 1, 0, 1, 0, 1, 0
            ), q1Dbg.states
        )

        q1Dbg.reset()
        q2Dbg.reset()
        q3Dbg.reset()
        q4Dbg.reset()

        dataIn.forceUpdate(1, 0, 1, 0)

        enable.forceUpdate(0)
        clear.forceUpdate(0)
        load.forceUpdate(1)
        pushClk(clk)

        assertEquals(listOf(1, 0, 1, 0), dataOut.bits())


        enable.forceUpdate(1)
        clear.forceUpdate(0)
        load.forceUpdate(0)
        pushClk(clk)
        pushClk(clk)
        pushClk(clk)

        assertEquals(listOf(0, 0, 0, 1), dataOut.bits())

        clear.forceUpdate(1)
        pushClk(clk)

        assertEquals(listOf(0, 0, 0, 0), dataOut.bits())

        assertEquals(listOf(1, 0), q4Dbg.states)
        assertEquals(listOf(1, 0), q3Dbg.states)
        assertEquals(listOf(1, 0), q2Dbg.states)
        assertEquals(listOf(1, 0, 1, 0), q1Dbg.states)
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
        assertEquals(listOf(0, 0, 0, 0), cnt.bits().reversed())

        pushClk(clk)
        assertEquals(listOf(0, 0, 0, 1), cnt.bits().reversed())

        pushClk(clk)
        assertEquals(listOf(0, 0, 1, 0), cnt.bits().reversed())

        pushClk(clk)
        assertEquals(listOf(0, 0, 1, 1), cnt.bits().reversed())

        pushClk(clk)
        assertEquals(listOf(0, 1, 0, 0), cnt.bits().reversed())

        pushClk(clk)
        assertEquals(listOf(0, 1, 0, 1), cnt.bits().reversed())

        pushClk(clk)
        assertEquals(listOf(0, 1, 1, 0), cnt.bits().reversed())

        pushClk(clk)
        assertEquals(listOf(0, 1, 1, 1), cnt.bits().reversed())

        pushClk(clk)
        assertEquals(listOf(1, 0, 0, 0), cnt.bits().reversed())

        for (i in 9..15) {
            pushClk(clk)
        }
        assertEquals(listOf(1, 1, 1, 1), cnt.bits().reversed())
        assertEquals(listOf(1, 0, 1), q4Dbg.states)
        assertEquals(listOf(1, 0, 1, 0, 1), q3Dbg.states)
        assertEquals(listOf(1, 0, 1, 0, 1, 0, 1, 0, 1), q2Dbg.states)
        assertEquals(
            listOf(
                1, 0, 1, 0, 1, 0, 1, 0, 1,
                0, 1, 0, 1, 0, 1, 0, 1
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
        j.forceUpdate(1)
        k.forceUpdate(0)
        pushClk(clk)
        assertEquals(1, q[0].signal)
        qDbg.reset()
        nqDbg.reset()

        pushClk(clk)
        assertEquals(1, q[0].signal)
        assertEquals(listOf<Boolean>(), qDbg.states)
        assertEquals(listOf<Boolean>(), nqDbg.states)

        pushClk(clk)
        pushClk(clk)
        assertEquals(1, q[0].signal)
        assertEquals(listOf<Boolean>(), qDbg.states)
        assertEquals(listOf<Boolean>(), nqDbg.states)
        //--------------------------------------
        // test state latch
        j.forceUpdate(0)
        k.forceUpdate(1)
        pushClk(clk)
        assertEquals(listOf(0), qDbg.states)
        assertEquals(listOf(1), nqDbg.states)

        pushClk(clk)
        assertEquals(listOf(0), qDbg.states)
        assertEquals(listOf(1), nqDbg.states)

        pushClk(clk)
        pushClk(clk)
        assertEquals(listOf(0), qDbg.states)
        assertEquals(listOf(1), nqDbg.states)
        //---------------------------------------
        // test state switching
        j.forceUpdate(1)
        k.forceUpdate(1)
        pushClk(clk)
        assertEquals(listOf(0, 1), qDbg.states)
        assertEquals(listOf(1, 0), nqDbg.states)

        pushClk(clk)
        pushClk(clk)
        pushClk(clk)
        assertEquals(listOf(0, 1, 0, 1, 0), qDbg.states)
        assertEquals(listOf(1, 0, 1, 0, 1), nqDbg.states)
        //---------------------------------------
    }

    @Test
    fun `andn test`() {
        val (inp, outp) = sigs(4, 1)
        andn(inp, outp, 4)
        inp.forceUpdate(1, 0, 0, 0)
        assertEquals(0, outp[0].signal)
        inp.forceUpdate(1, 0, 1, 0)
        assertEquals(0, outp[0].signal)
        inp.forceUpdate(1, 1, 1, 0)
        assertEquals(0, outp[0].signal)

        inp.forceUpdate(1, 1, 1, 1)
        assertEquals(1, outp[0].signal)
    }

    @Test
    fun `decoder test`() {
        val (inp, outp) = sigs(5, 16)
        decoder(inp, outp, 4)
        inp.forceUpdate(1, 0, 0, 0, 0)
        assertEquals(
            listOf(
                1, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            ),
            outp.bits()
        )


        inp.forceUpdate(1, 1, 0, 0, 0)
        assertEquals(
            listOf(
                0, 1, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            ),
            outp.bits()
        )

        inp.forceUpdate(1, 0, 0, 0, 1)
        assertEquals(
            listOf(
                0, 0, 0, 0,
                0, 0, 0, 0,
                1, 0, 0, 0,
                0, 0, 0, 0
            ),
            outp.bits()
        )

        inp.forceUpdate(1, 1, 1, 1, 1)
        assertEquals(
            listOf(
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 1
            ),
            outp.bits()
        )

        inp.forceUpdate(0, 1, 1, 1, 1)
        assertEquals(
            listOf(
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            ),
            outp.bits()
        )
    }

    @Test
    fun `memory test`() {
        val (wr, rd, addr, bus) = sigs(1, 1, 4, 8)
        memory8Bit(wr + rd + addr + bus, bus, 4)

        rd.forceUpdate(0)
        bus.forceUpdate(1, 0, 1, 0, 1, 0, 1, 0)
        addr.forceUpdate(0, 1, 0, 0)
        wr.forceUpdate(1)

        wr.forceUpdate(0)
        rd.forceUpdate(0)
        bus.forceUpdate(0, 0, 0, 0, 0, 0, 0, 0)

        rd.forceUpdate(1)

        assertEquals(
            listOf(1, 0, 1, 0, 1, 0, 1, 0),
            bus.bits()
        )

        addr.forceUpdate(0, 0, 0, 0)

        assertEquals(
            listOf(0, 0, 0, 0, 0, 0, 0, 0),
            bus.bits()
        )

        addr.forceUpdate(0, 1, 0, 0)
        assertEquals(
            listOf(1, 0, 1, 0, 1, 0, 1, 0),
            bus.bits()
        )

        rd.forceUpdate(0)
        bus.forceUpdate(0, 0, 0, 0, 0, 0, 0, 0)
        addr.forceUpdate(0, 0, 0, 0)
        assertEquals(
            listOf(0, 0, 0, 0, 0, 0, 0, 0),
            bus.bits()
        )
        addr.forceUpdate(0, 1, 0, 0)
        assertEquals(
            listOf(0, 0, 0, 0, 0, 0, 0, 0),
            bus.bits()
        )

        bus.forceUpdate(0, 1, 1, 0, 0, 0, 0, 0)
        addr.forceUpdate(0, 1, 0, 0)
        wr.forceUpdate(1)
        wr.forceUpdate(0)
        bus.forceUpdate(0, 0, 0, 0, 0, 0, 0, 0)
        rd.forceUpdate(1)
        assertEquals(
            listOf(0, 1, 1, 0, 0, 0, 0, 0),
            bus.bits()
        )

    }

    private fun pushClk(clcIn: List<Signal>) {
        clcIn.forceUpdate(1)
        clcIn.forceUpdate(0)
        println("---")
    }

}
