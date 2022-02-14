package org.ebics.client.certificate

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.*
import java.net.URL
import java.security.Security
import java.security.interfaces.RSAPublicKey


class KeyUtilTest {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    private fun getResourceURL(path: String): URL {
        val resource = object {}.javaClass.getResource(path)
        if (resource != null)
            return resource
        else
            throw IllegalArgumentException("The requested resource URL '$path' doesn't exist")
    }

    private fun getResourceAsStream(path: String): InputStream? =
        getResourceURL(path).openStream()

    private fun getResourceAsFile(path: String): File =
        File(getResourceURL(path).file)

    @Test
    fun testGenerateDigestFromFiles() {
        getResourceAsFile("/testPublicKeys/").walk().filter { it.isFile }.forEach { pubFile ->
            val pubKey = KeyUtil.loadKey(FileInputStream(pubFile))
            val hash = pubFile.nameWithoutExtension.removePrefix("key-")
            Assertions.assertEquals(hash, String(KeyUtil.getKeyHash(pubKey as RSAPublicKey)))
            println(hash)
        }
    }

    @Test
    fun testGenerateAndSaveAndLoadAndCompareDigest() {
        val kp = KeyUtil.makeKeyPair(2048)
        val digest = String(KeyUtil.getKeyHash(kp.public as RSAPublicKey))
        println(digest)
        val bos = ByteArrayOutputStream()
        KeyUtil.saveKey(bos, kp.public)
        val publicKey = KeyUtil.loadKey(ByteArrayInputStream(bos.toByteArray()))
        val digest2 = String(KeyUtil.getKeyHash(publicKey as RSAPublicKey))
        Assertions.assertEquals(digest2, digest)
    }

    /*@Test
    fun createTestFileAndStoreIt() {
        val kp = KeyUtil.makeKeyPair(2048)
        val digest = String(KeyUtil.getKeyHash(kp.public as RSAPublicKey))
        println(digest)
        val fos = FileOutputStream("/tmp/key-$digest.pub")
        KeyUtil.saveKey(fos, kp.public)
    }*/

}