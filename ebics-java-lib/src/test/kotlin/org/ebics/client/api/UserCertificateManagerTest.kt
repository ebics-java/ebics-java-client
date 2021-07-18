package org.ebics.client.api

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.certificate.UserCertificateManager
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
    fun testCreateSaveAndLoadCertificates() {
        //Create and save
        val manager = UserCertificateManager.create("cn=test,o=google,c=de")
        val bos = ByteArrayOutputStream()
        manager.save(bos, "pass"::toCharArray, "user1")

        //Load certificates
        val ins = ByteArrayInputStream(bos.toByteArray())
        val managerLoaded = UserCertificateManager.load(ins, "pass"::toCharArray, "user1")

        Assertions.assertArrayEquals(manager.a005Certificate.encoded, managerLoaded.a005Certificate.encoded)
        Assertions.assertArrayEquals(manager.x002Certificate.encoded, managerLoaded.x002Certificate.encoded)
        Assertions.assertArrayEquals(manager.e002Certificate.encoded, managerLoaded.e002Certificate.encoded)

        Assertions.assertArrayEquals(manager.a005PrivateKey.encoded, managerLoaded.a005PrivateKey.encoded)
        Assertions.assertArrayEquals(manager.x002PrivateKey.encoded, managerLoaded.x002PrivateKey.encoded)
        Assertions.assertArrayEquals(manager.e002PrivateKey.encoded, managerLoaded.e002PrivateKey.encoded)
    }

    @Test
    fun testCreateAndSign() {
        //Create and save
        val manager = UserCertificateManager.create("cn=test,o=google,c=de")
        val test = "teststr"

        //SHA256withRSA
        val signed = manager.sign(test.toByteArray())

        //SHA256
        val sha256 = manager.createSHA256hash(test.toByteArray())
        //+withRSA
        val signed2 = manager.signSHA256hash(sha256)

        Assertions.assertArrayEquals(signed, signed2)
    }
}