package org.kopi.ebics.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReturnCodeTest {

    @Test
    void test() {
        assertEquals(0, ReturnCode.EBICS_OK.getCode());
        assertEquals("EBICS_OK", ReturnCode.EBICS_OK.getSymbolicName());
    }
}
