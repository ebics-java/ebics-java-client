package org.ebics.client.certificate

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.Security

class UserCertificateManagerTest {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun testGenerateSaveAndLoadUserCert() {
        val userCertMan = UserCertificateManager.create("cn=test")
        val fos = ByteArrayOutputStream()
        userCertMan.save(fos, "pass", "prefix1")
        val loadedUserCertMan = UserCertificateManager.load(ByteArrayInputStream(fos.toByteArray()), "pass", "prefix1")

        Assertions.assertArrayEquals(userCertMan.a005PrivateKey.encoded, loadedUserCertMan.a005PrivateKey.encoded)
        Assertions.assertArrayEquals(userCertMan.x002PrivateKey.encoded, loadedUserCertMan.x002PrivateKey.encoded)
        Assertions.assertArrayEquals(userCertMan.e002PrivateKey.encoded, loadedUserCertMan.e002PrivateKey.encoded)

        Assertions.assertArrayEquals(userCertMan.a005Certificate.encoded, loadedUserCertMan.a005Certificate.encoded)
        Assertions.assertArrayEquals(userCertMan.x002Certificate.encoded, loadedUserCertMan.x002Certificate.encoded)
        Assertions.assertArrayEquals(userCertMan.e002Certificate.encoded, loadedUserCertMan.e002Certificate.encoded)
    }
}