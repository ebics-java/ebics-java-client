package org.ebics.client.api.trace

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatusEnum
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
class TraceEntryAccessTest {
    private fun getMockUser(creator: String): BankConnectionEntity {
        val bank = Bank(null, URL("https://test.com"), "id", "name", null)
        val partner = Partner(null, bank, "1", 1)
        return BankConnectionEntity(null, EbicsVersion.H005, "1", "1", "cn=jan", EbicsUserStatusEnum.CREATED, false, false, partner, null, creator, false)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun createTraceEntry_then_defaultCreatorIsSameAsUserContext() {
        val te = TraceEntry(1, "mb", getMockUser("jan"), "sessId1", "O5N3", EbicsVersion.H004, false)
        Assertions.assertEquals("jan", te.creator)
        Assertions.assertEquals("jan", te.getOwnerName())
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun createTraceEntryWithNonDefaultUserPeter_then_creatorIsPeter() {
        val te = TraceEntry(1, "mb", getMockUser("peter"), "sessId1", "O5N3", EbicsVersion.H004, false, creator = "peter")
        Assertions.assertEquals("peter", te.creator)
        Assertions.assertEquals("peter", te.getOwnerName())
    }

    @Test
    @WithMockUser(username = "peter", roles = ["USER", "ADMIN"])
    fun createTraceEntryAsJan_canNOT_be_readByPeter() {
        val te = TraceEntry(1, "mb", getMockUser("jan"), "sessId1", "O5N3", EbicsVersion.H004, false, creator = "jan")
        Assertions.assertFalse(te.hasReadAccess())
        Assertions.assertThrows(IllegalAccessException::class.java) {
            te.checkReadAccess()
        }
    }
}