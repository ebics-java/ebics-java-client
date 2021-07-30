package org.ebics.client.api.user.cert

import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.api.user.UserPartnerBank
import org.ebics.client.api.user.UserService
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.model.EbicsVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
@AutoConfigurationPackage(basePackages = ["org.ebics.client.api.*"])
@ContextConfiguration(classes = [UserKeyStoreService::class, UserService::class, PartnerService::class, BankService::class])
open class UserKeyStoreServiceTest (
    @Autowired private val userService: UserService,
    @Autowired private val bankService: BankService,
    @Autowired private val keyStoreService: UserKeyStoreService)
{
    @Test
    fun createStoreAndLoad() {
        //Create and store bank, partner, user
        val bank = Bank(null, URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH", null)
        val bankId = bankService.createBank(bank)
        val userInfo = UserPartnerBank( EbicsVersion.H005, "CHT10001", "Jan", "cn=jan", "CH100001", bankId,
            useCertificate = true,
            usePassword = true,
            guestAccess = true
        )
        val userId = userService.createUserAndPartner(userInfo)
        val user = userService.getUserById(userId)

        //Create and store user certificate
        val certificates = UserCertificateManager.create(userInfo.dn)
        val pass = "testPass"
        val userKeyStore = UserKeyStore.fromUserCertMgr(user,certificates, pass)
        val keyStoreId = keyStoreService.save( userKeyStore )

        //Load stored certificates from DB
        val userKeyStoreLoaded = keyStoreService.loadById(keyStoreId)
        val loadedCertificates = userKeyStoreLoaded.toUserCertMgr( pass )

        //Compare created & loaded certs
        with(loadedCertificates) {
            assertThat(a005Certificate.encoded).isEqualTo(certificates.a005Certificate.encoded)
            assertThat(x002Certificate.encoded).isEqualTo(certificates.x002Certificate.encoded)
            assertThat(e002Certificate.encoded).isEqualTo(certificates.e002Certificate.encoded)
            assertThat(a005PrivateKey.encoded).isEqualTo(certificates.a005PrivateKey.encoded)
            assertThat(x002PrivateKey.encoded).isEqualTo(certificates.x002PrivateKey.encoded)
            assertThat(e002PrivateKey.encoded).isEqualTo(certificates.e002PrivateKey.encoded)
        }
    }
}