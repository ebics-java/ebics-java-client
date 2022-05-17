package org.ebics.client.api.bankconnection.cert

import DbTestContext
import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankRepository
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.partner.PartnerRepository
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionRepository
import org.ebics.client.model.EbicsVersion
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
class UserKeyStoreRepositoryTest (
    @Autowired private val bankRepo: BankRepository,
    @Autowired private val partnerRepo: PartnerRepository,
    @Autowired private val userRepo: BankConnectionRepository,
    @Autowired private val keyStoreRepo: UserKeyStoreRepository)
{
    @Test
    fun testCreateStoreAndLoad() {
        val bank = Bank(null, URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH", null)
        bankRepo.save(bank)
        val partner = Partner(null, bank, "CH100001", 0)
        partnerRepo.save(partner)
        val user = BankConnectionEntity(null, EbicsVersion.H005, "CHT10001", "Jan", "org=jto", keyStore = null, partner = partner, usePassword = false, useCertificate = true, creator = "Jan", guestAccess = true)
        userRepo.saveAndFlush(user)
        val userId:Long = requireNotNull(user.id) {"User id must be not null"}
        val userKeyStore = UserKeyStore(null, "abc".toByteArray(), user)
        keyStoreRepo.saveAndFlush(userKeyStore)
        user.keyStore = userKeyStore
        userRepo.saveAndFlush(user)
        val loadedKeyStore = keyStoreRepo.getKeyStoreByUserId(userId)
        assertThat(loadedKeyStore.user.name).isEqualTo(user.name)
        assertThat(loadedKeyStore.id).isEqualTo(userKeyStore.id)
        assertThat(loadedKeyStore.keyStoreBytes).isEqualTo(userKeyStore.keyStoreBytes)
    }
}