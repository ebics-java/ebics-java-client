package org.ebics.client.api.bankconnection

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class DomainNameGeneratorTest {
    @Test
    fun testDomainNameGeneratorNameOnly() {
        val dn = DomainNameGenerator("my -*/Nameüü£$", null)
        Assertions.assertEquals("cn=my-name", dn.toString())
    }

    @Test
    fun testDomainNameGeneratorNameEmptyOnly() {
        val dn = DomainNameGenerator("-*/£$", null)
        Assertions.assertEquals("", dn.toString())
    }

    @Test
    fun testDomainNameGeneratorNameAndCountry() {
        val dn = DomainNameGenerator("my -*/Nameüü£$", "cz-/=**=¨")
        Assertions.assertEquals("cn=my-name,c=cz", dn.toString())
    }
}