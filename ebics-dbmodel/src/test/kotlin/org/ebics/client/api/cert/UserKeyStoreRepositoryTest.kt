package org.ebics.client.api.cert

import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.user.User
import org.ebics.client.api.user.UserRepository
import org.ebics.client.model.EbicsVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
class UserKeyStoreRepositoryTest (
    @Autowired private val userRepo: UserRepository,
    @Autowired private val keyStoreRepo: UserKeyStoreRepository)
{
    @Test
    fun testCreateStoreAndLoad() {
        val bank = Bank(null, URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), true,"EBXUBSCH", "UBS-PROD-CH")
        val partner = Partner(null, bank, "CH100001", 0)
        val user = User(null, EbicsVersion.H005, "CHT10001", "Jan", "org=jto", keyStore = null, partner = partner)
        userRepo.saveAndFlush(user)
        val userId:Long = requireNotNull(user.id) {"User id must be not null"}
        val userKeyStore = UserKeyStore(null, "abc".toByteArray(), user)
        keyStoreRepo.saveAndFlush(userKeyStore)
        val loadedKeyStore = keyStoreRepo.getKeyStoreByUserId(userId)
        assertThat(loadedKeyStore.user.name).isEqualTo(user.name)
        assertThat(loadedKeyStore.id).isEqualTo(userKeyStore.id)
        assertThat(loadedKeyStore.keyStoreBytes).isEqualTo(userKeyStore.keyStoreBytes)
    }
}