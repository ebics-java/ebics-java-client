package org.ebics.client.api.partner

import org.ebics.client.api.user.cert.UserKeyStoreService
import org.ebics.client.api.user.UserServiceImpl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataJpaTest
@AutoConfigurationPackage(basePackages = ["org.ebics.client.api.*"])
@ContextConfiguration(classes = [UserKeyStoreService::class, UserServiceImpl::class])
class PartnerRepositoryTest(@Autowired private val partnerService: PartnerService) {
    @Test
    fun testAddAndDeletePartner() {

    }
}