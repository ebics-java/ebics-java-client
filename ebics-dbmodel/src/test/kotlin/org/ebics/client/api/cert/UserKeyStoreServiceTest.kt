package org.ebics.client.api.cert

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.apache.xml.security.Init
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.user.User
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
import java.security.Security

@ExtendWith(SpringExtension::class)
@DataJpaTest
@AutoConfigurationPackage(basePackages = ["org.ebics.client.api.*"])
@ContextConfiguration(classes = [UserKeyStoreService::class, UserService::class])
open class UserKeyStoreServiceTest (
    @Autowired private val userService: UserService,
    @Autowired private val keyStoreService: UserKeyStoreService)
{
    init {
        Init.init()
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun createStoreAndLoad() {
        val bank = Bank(null, URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), true,"EBXUBSCH", "UBS-PROD-CH")
        val partner = Partner(null, bank, "CH100001", 0)
        val user = User(null, EbicsVersion.H005, "CHT10001", "Jan", "org=jto", keyStore = null, partner = partner)
        userService.createUser(user)
        val certificates = UserCertificateManager.create(user.dn)
        val bos = ByteOutputStream(4096)
        val pass = "testPass"
        certificates.save(bos, pass::toCharArray, user.userId)
        keyStoreService.save(bos, user)

        val bis = keyStoreService.load(requireNotNull(user.id) {"User id must not be null"})
        val loadedCertificates = UserCertificateManager.load(bis, pass::toCharArray, user.userId)
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