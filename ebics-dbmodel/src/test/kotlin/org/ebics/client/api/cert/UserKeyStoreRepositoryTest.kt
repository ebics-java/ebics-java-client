package org.ebics.client.api.cert

import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.user.UserInfo
import org.ebics.client.api.user.UserInfoRepository
import org.ebics.client.api.user.UserStatus
import org.ebics.client.model.EbicsVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataJpaTest
class UserKeyStoreRepositoryTest (
    @Autowired private val userRepo: UserInfoRepository,
    @Autowired private val keyStoreRepo: UserKeyStoreRepository)
{
    @Test
    fun testCreateStoreAndLoad() {
        val user = UserInfo(null, EbicsVersion.H005, "CHT10001", "Jan", "org=jto", keyStore = null)
        userRepo.saveAndFlush(user)
        val userId:Long = requireNotNull(user.id) {"User id must be not null"}
        val userKeyStore = UserKeyStore(null, "abc".toByteArray(), user)
        keyStoreRepo.saveAndFlush(userKeyStore)
        val loadedKeyStore = keyStoreRepo.getKeyStoreByUserId(userId)
        assertThat(loadedKeyStore.userInfo.name).isEqualTo(user.name)
        assertThat(loadedKeyStore.id).isEqualTo(userKeyStore.id)
        assertThat(loadedKeyStore.keyStoreBytes).isEqualTo(userKeyStore.keyStoreBytes)
    }
}