package org.ebics.client.api.partner

import DbTestContext
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [DbTestContext::class])
class PartnerRepositoryTest(
    @Autowired private val bankService: BankService,
    @Autowired private val partnerService: PartnerService
) {
    @Test
    fun testAddAndGetPartner() {
        val bankData = BankData(URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bankData)
        val bank = bankService.getBankById(bankId)
        val partner = Partner(null, bank, "CH100001", 0)
        val partnerId = partnerService.addPartner(partner)
        Assertions.assertNotNull(partnerId)
        val partner2 = partnerService.getPartnerById(partnerId)
        Assertions.assertEquals(partner, partner2)
    }

    @Test
    fun testAddAndDeletePartner() {
        val bankData = BankData(URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bankData)
        val bank = bankService.getBankById(bankId)
        val partner = Partner(null, bank, "CH100001", 0)
        val partnerId = partnerService.addPartner(partner)
        Assertions.assertNotNull(partnerId)
        partnerService.deletePartnerById(partnerId)
    }

    @Test
    fun testCreateOrGetPartner_doesntReturnNull() {
        val bankData = BankData(URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bankData)
        val partnerId = partnerService.createOrGetPartner("CH10001", bankId)
        Assertions.assertNotNull(partnerId)
    }

    @Test
    fun testCreateOrGetPartnerTwice_returnSameId() {
        val bankData = BankData(URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bankData)
        val partnerId = partnerService.createOrGetPartner("CH10001", bankId)
        Assertions.assertNotNull(partnerId)
        val partnerId2 = partnerService.createOrGetPartner("CH10001", bankId)
        Assertions.assertEquals(partnerId, partnerId2)
    }

    @Test
    fun testCreateOrGetPartnerTwiceDifferent_returnDifferentId() {
        val bankData = BankData(URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bankData)
        val partnerId = partnerService.createOrGetPartner("CH10001", bankId)
        Assertions.assertNotNull(partnerId)
        val partnerId2 = partnerService.createOrGetPartner("CH1XXXX", bankId)
        Assertions.assertNotEquals(partnerId, partnerId2)
    }
}