package com.dzharvis

import junit.framework.Assert.assertEquals
import org.junit.Test

class UtilsTest {
    @Test
    fun test() {
        val divider = nBitBinaryCounterSim(4)
        assertEquals(listOf(false, false, false, false), divider()) //binary zero for first call
        for (i in 0 until 8) divider()

        // binary nine (1001) after 9 calls
        assertEquals(listOf(true, false, false, true), divider())
    }
}