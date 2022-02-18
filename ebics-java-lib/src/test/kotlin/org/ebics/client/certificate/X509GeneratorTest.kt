package org.ebics.client.certificate

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class X509GeneratorTest {
    @Test
    fun generateA005() {
        val kp = KeyUtil.makeKeyPair(2048)
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION)
        val endDate = Date(calendar.timeInMillis)
        val startDate = Date()
        val userDn = "CN=test"
        val a005 =
            X509Generator.generateA005Certificate(kp, userDn, startDate, endDate)
        println(a005.toString())
        Assertions.assertNotNull(a005)
        Assertions.assertEquals(userDn, a005.issuerDN.name)
        Assertions.assertEquals(userDn, a005.subjectDN.name)
        Assertions.assertArrayEquals(arrayOf(false, true, false, false, false, false, false, false, false), a005.keyUsage.toTypedArray())
    }

    @Test
    fun generateX002() {
        val kp = KeyUtil.makeKeyPair(2048)
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION)
        val endDate = Date(calendar.timeInMillis)
        val startDate = Date()
        val userDn = "CN=test"
        val x002 =
            X509Generator.generateX002Certificate(kp, userDn, startDate, endDate)
        println(x002.toString())
        Assertions.assertNotNull(x002)
        Assertions.assertEquals(userDn, x002.issuerDN.name)
        Assertions.assertEquals(userDn, x002.subjectDN.name)
        Assertions.assertArrayEquals(arrayOf(true, false, false, false, false, false, false, false, false), x002.keyUsage.toTypedArray())
    }

    @Test
    fun generateE002() {
        val kp = KeyUtil.makeKeyPair(2048)
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION)
        val endDate = Date(calendar.timeInMillis)
        val startDate = Date()
        val userDn = "CN=test"
        val e002 =
            X509Generator.generateE002Certificate(kp, userDn, startDate, endDate)
        println(e002.toString())
        Assertions.assertNotNull(e002)
        Assertions.assertEquals(userDn, e002.issuerDN.name)
        Assertions.assertEquals(userDn, e002.subjectDN.name)
        Assertions.assertArrayEquals(arrayOf(false, false, false, false, true, false, false, false, false), e002.keyUsage.toTypedArray())
    }
}