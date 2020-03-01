package com.dzharvis

import junit.framework.Assert.assertEquals
import org.junit.Test
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
}