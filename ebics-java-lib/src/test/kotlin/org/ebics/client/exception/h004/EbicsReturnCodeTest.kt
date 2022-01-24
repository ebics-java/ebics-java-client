package org.ebics.client.exception.h004

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EbicsReturnCodeTest {
    @Test
    fun testReturnCode() {
        val ok = EbicsReturnCode("000000", "OK")
        Assertions.assertTrue(ok.isOk)
    }
}