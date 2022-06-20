package org.ebics.client.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.exception.EbicsException
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    /**
     * Encrypts an input with a given key spec.
     *
     *
     * EBICS Specification 2.4.2 - 15.1 Workflows at the sender’s end:
     *
     *
     * **Preparation for DEK encryption**
     *
     * The 128 bit DEK that is interpreted as a natural number is filled out with null bits to 768 bits in
     * front of the highest-value bit. The result is called PDEK.
     *
     *
     * **Encryption of the secret DES key**
     *
     * PDEK is then encrypted with the recipient’s public key of the RSA key system and is then
     * expanded with leading null bits to 1024 bits.
     *
     * The result is called EDEK. It must be ensured that EDEK is not equal to DEK.
     *
     *
     * **Encryption of the messages**
     *
     * <U>Padding of the message:</U>
     *
     * The method Padding with Octets in accordance with ANSI X9.23 is used for padding the
     * message, i.e. in all cases, data is appended to the message that is to be encrypted.
     *
     *
     * <U>Application of the encryption algorithm:</U>
     *
     * The message is encrypted in CBC mode in accordance with ANSI X3.106 with the secret key
     * DEK according to the 2-key triple DES process as specified in ANSI X3.92-1981.
     *
     * In doing this, the following initialization value “ICV” is used: X ‘00 00 00 00 00 00 00 00’.
     *
     * @param input   the input to encrypt
     * @param keySpec the key spec
     * @return the encrypted input
     * @throws EbicsException
     */
    @JvmStatic
    @Throws(EbicsException::class)
    fun encrypt(input: ByteArray, keySpec: SecretKeySpec): ByteArray {
        return encryptOrDecrypt(Cipher.ENCRYPT_MODE, input, keySpec)
    }

    /**
     * Decrypts the given input according to key spec.
     *
     * @param input   the input to decrypt
     * @param keySpec the key spec
     * @return the decrypted input
     * @throws EbicsException
     */
    @Throws(EbicsException::class)
    fun decrypt(input: ByteArray, keySpec: SecretKeySpec): ByteArray {
        return encryptOrDecrypt(Cipher.DECRYPT_MODE, input, keySpec)
    }

    /**
     * Encrypts or decrypts the given input according to key spec.
     *
     * @param mode    the encryption-decryption mode.
     * @param input   the input to encrypt or decrypt.
     * @param keySpec the key spec.
     * @return the encrypted or decrypted data.
     * @throws GeneralSecurityException
     */
    @Throws(EbicsException::class)
    private fun encryptOrDecrypt(mode: Int, input: ByteArray, keySpec: SecretKeySpec): ByteArray {
        val cipher: Cipher
        val iv = IvParameterSpec(ByteArray(16))
        return try {
            cipher = Cipher.getInstance("AES/CBC/ISO10126Padding", BouncyCastleProvider.PROVIDER_NAME)
            cipher.init(mode, keySpec, iv)
            cipher.doFinal(input)
        } catch (e: GeneralSecurityException) {
            throw EbicsException(e.message)
        }
    }
}