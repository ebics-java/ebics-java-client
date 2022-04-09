package org.ebics.client.certificate

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.Security

class UserMultiCertManagerTest {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun testCreateCertMan_mustBeEmpty() {
        val userCertMan = EbicsUserCertificateManager.createEmpty()
        Assertions.assertTrue(userCertMan.isEmpty())
    }

    @Test
    fun testAddTwoCertWithSameDn_mustCauseException() {
        val userCertMan = EbicsUserCertificateManager.createEmpty()
        userCertMan.add("cn=test1")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            userCertMan.add("cn=test1")
        }
    }

    @Test
    fun testGenerateSaveAndLoadUserCert() {
        val userCertMan = EbicsUserCertificateManager.createEmpty()
        userCertMan.add("cn=test1")
        userCertMan.add("cn=test2")
        val fos = ByteArrayOutputStream()
        userCertMan.save(fos, "pass")
        val loadedUserCertMan = EbicsUserCertificateManager.load(ByteArrayInputStream(fos.toByteArray()), "pass")
        Assertions.assertEquals(userCertMan.size, loadedUserCertMan.size)
        Assertions.assertEquals(userCertMan["cn=test1"], loadedUserCertMan["cn=test1"])
        Assertions.assertEquals(userCertMan["cn=test2"], loadedUserCertMan["cn=test2"])
    }
}