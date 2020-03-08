package com.dzharvis

import junit.framework.Assert.assertEquals
import org.junit.Test
import utils.binToDec
import utils.nBitBinaryCounterSim
import utils.unzip

class UtilsTest {
    @Test
    fun `binary counter simulation`() {
        val divider = nBitBinaryCounterSim(4)
        assertEquals(listOf(0, 0, 0, 0), divider()) //binary zero for first call
        for (i in 0 until 8) divider()

        // binary nine (1001) after 9 calls
        assertEquals(listOf(1, 0, 0, 1), divider())
    }

    @Test
    fun `test unzip`() {
        val unzip = unzip(listOf(-1, 0, 0, -1))
        assertEquals(listOf(
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 1),
            listOf(1, 0, 0, 0),
            listOf(1, 0, 0, 1)
        ), unzip)
    }

    @Test
    fun `test bin to dec`() {
        assertEquals(10, binToDec(listOf(0, 1, 0, 1)))
        assertEquals(0, binToDec(listOf(0, 0, 0, 0)))
        assertEquals(11, binToDec(listOf(1, 1, 0, 1)))
        assertEquals(15, binToDec(listOf(1, 1, 1, 1)))
    }
}