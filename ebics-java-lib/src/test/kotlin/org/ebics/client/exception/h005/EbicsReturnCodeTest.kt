package org.ebics.client.exception.h005

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EbicsReturnCodeTest {
    @Test
    fun testOkReturnCode() {
        val ok = EbicsReturnCode("000000", "OK")
        Assertions.assertTrue(ok.isOk)
    }

    @Test
    fun testErrorReturnCode() {
        val ok = EbicsReturnCode("000001", "OK")
        Assertions.assertFalse(ok.isOk)
    }
}