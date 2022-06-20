package org.ebics.client.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.security.Security
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class CryptoUtilsTest {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun test0() {
        encryptAndDecrypt_thenResultIsSameAsInput(Random.nextBytes(0));
    }

    @Test
    fun test1() {
        encryptAndDecrypt_thenResultIsSameAsInput(Random.nextBytes(100));
    }

    @Test
    fun test2() {
        encryptAndDecrypt_thenResultIsSameAsInput(Random.nextBytes(1000));
    }

    private fun encryptAndDecrypt_thenResultIsSameAsInput(inputByteArray: ByteArray) {
        val nonce = Utils.generateNonce()
        val keySpec = SecretKeySpec(nonce, "EAS")
        val encryptedInput = CryptoUtils.encrypt(inputByteArray, keySpec)
        val decryptedInput = CryptoUtils.decrypt(encryptedInput, keySpec)
        Assertions.assertArrayEquals(inputByteArray, decryptedInput)
    }
}