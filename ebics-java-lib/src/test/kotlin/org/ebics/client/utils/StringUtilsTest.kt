package org.ebics.client.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StringUtilsTest {
    @Test
    fun testHex() {
        val test = "abcdeffdsa5546"
        val byteArray = test.toByteArray()
        Assertions.assertArrayEquals( byteArray, byteArray.toHexString().decodeHexToByteArray())
    }
}