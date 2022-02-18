/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.ebics.client.certificate

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.exception.EbicsException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.security.*
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

/**
 * Some key utilities
 *
 * @author hachani
 */
object KeyUtil {
    /**
     * Generates a `KeyPair` in RSA format.
     *
     * @param keyLen - key size
     * @return KeyPair the key pair
     * @throws NoSuchAlgorithmException
     */
    @Throws(NoSuchAlgorithmException::class)
    fun makeKeyPair(keyLen: Int): KeyPair {
        val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(keyLen, SecureRandom())
        return keyGen.generateKeyPair()
    }

    /**
     * Generates a random password
     *
     * @return the password
     */
    fun generatePassword(): String {
        val random: SecureRandom
        return try {
            random = SecureRandom.getInstance("SHA1PRNG")
            val pwd = Base64.encodeBase64String(random.generateSeed(5))
            pwd.substring(0, pwd.length - 2)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Returns the digest value of a given public key.
     *
     * In Version “H003” of the EBICS protocol the ES of the financial:
     *
     * The SHA-256 hash values of the financial institution's public keys for X002 and E002 are
     * composed by concatenating the exponent with a blank character and the modulus in hexadecimal
     * representation (using lower case letters) without leading zero (as to the hexadecimal
     * representation). The resulting string has to be converted into a byte array based on US ASCII
     * code.
     *
     * @param publicKey the public key
     * @return the digest value
     * @throws EbicsException
     */
    @Throws(EbicsException::class)
    fun getKeyHash(publicKey: RSAPublicKey): ByteArray {
        val exponent: String = Hex.encodeHexString(publicKey.publicExponent.toByteArray())
        val modulus: String = Hex.encodeHexString(removeFirstByte(publicKey.modulus.toByteArray()))
        val hash: String = "$exponent $modulus".removePrefix("0")
        val digest: ByteArray = try {
            MessageDigest.getInstance("SHA-256", BouncyCastleProvider.PROVIDER_NAME).digest(hash.toByteArray(charset("US-ASCII")))
        } catch (e: GeneralSecurityException) {
            throw EbicsException(e.message)
        } catch (e: UnsupportedEncodingException) {
            throw EbicsException(e.message)
        }
        return String(Hex.encodeHex(digest, false)).toByteArray()
    }

    /**
     * Remove the first byte of an byte array
     *
     * @return the array
     */
    private fun removeFirstByte(byteArray: ByteArray): ByteArray {
        return byteArray.copyOfRange(1, byteArray.size)
    }

    /**
     * Returns the certificate hash
     * @param certificate the certificate
     * @return the certificate hash
     * @throws GeneralSecurityException
     */
    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun getCertificateHash(certificate: ByteArray): ByteArray {
        val hash256 = String(Hex.encodeHex(MessageDigest.getInstance("SHA-256").digest(certificate), false))
        return format(hash256).toByteArray()
    }

    /**
     * Formats a hash 256 input.
     * @param hash256 the hash input
     * @return the formatted hash
     */
    private fun format(hash256: String): String {
        val formatted: String
        val buffer = StringBuffer()
        var i = 0
        while (i < hash256.length) {
            buffer.append(hash256[i])
            buffer.append(hash256[i + 1])
            buffer.append(' ')
            i += 2
        }
        formatted = buffer.substring(0, 48) + LINE_SEPARATOR + buffer.substring(48) + LINE_SEPARATOR
        return formatted
    }

    fun saveKey(fos: OutputStream, key: Key) {
        val x509EncodedKeySpec = X509EncodedKeySpec(key.encoded)
        fos.write(x509EncodedKeySpec.encoded)
        fos.close()
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun loadKey(fis: InputStream): Key {
        val encodedPublicKey = ByteArray(10000)
        fis.read(encodedPublicKey)
        fis.close()
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val publicKeySpec = X509EncodedKeySpec(encodedPublicKey)
        return keyFactory.generatePublic(publicKeySpec)
    }

    private val LINE_SEPARATOR: String = System.getProperty("line.separator")
}