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
package org.ebics.client.letter

import org.apache.commons.codec.binary.Hex
import org.ebics.client.exception.EbicsException
import org.ebics.client.api.InitLetter
import org.ebics.client.messages.Messages
import java.io.*
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.interfaces.RSAPublicKey
import java.util.*

abstract class AbstractInitLetter(
    protected val locale: Locale,
    hostId: String,
    bankName: String,
    userId: String,
    username: String,
    partnerId: String,
    version: String,
    certTitle: String,
    certificate: ByteArray?,
    hashTitle: String,
    hash: ByteArray
) : InitLetter {

    private val letter: Letter = Letter(
        title, hostId, bankName, userId, username, partnerId,
        version, certTitle, certificate, hashTitle, hash, locale
    )

    @Throws(IOException::class)
    override fun writeTo(output: OutputStream) {
        output.write(letter.getLetterBytes())
    }

    companion object {
        @JvmStatic
        @Throws(EbicsException::class)
        protected fun getHash(publicKey: RSAPublicKey): ByteArray {
            var hash: String
            val exponent: String = Hex.encodeHexString(publicKey.publicExponent.toByteArray())
            val modulus: String = Hex.encodeHexString(removeFirstByte(publicKey.modulus.toByteArray()))
            hash = "$exponent $modulus"
            if (hash[0] == '0') {
                hash = hash.substring(1)
            }
            val digest: ByteArray = try {
                MessageDigest.getInstance("SHA-256", "BC").digest(hash.toByteArray(charset("US-ASCII")))
            } catch (e: GeneralSecurityException) {
                throw EbicsException(e.message)
            } catch (e: UnsupportedEncodingException) {
                throw EbicsException(e.message)
            }
            return format(String(Hex.encodeHex(digest, false))).toByteArray()
        }

        /**
         * Returns the certificate hash
         * @param certificate the certificate
         * @return the certificate hash
         * @throws GeneralSecurityException
         */
        @JvmStatic
        @Throws(GeneralSecurityException::class)
        protected fun getHash(certificate: ByteArray): ByteArray {
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

        private fun removeFirstByte(byteArray: ByteArray): ByteArray {
            val b = ByteArray(byteArray.size - 1)
            System.arraycopy(byteArray, 1, b, 0, b.size)
            return b
        }

        /**
         * Returns the value of the property key.
         * @param key the property key
         * @param bundleName the bundle name
         * @param locale the bundle locale
         * @return the property value
         */
        @JvmStatic
        protected fun getString(key: String, bundleName: String, locale: Locale): String =
            Messages.getString(key, bundleName, locale)

        @JvmStatic
        protected val BUNDLE_NAME = "org.ebics.client.letter.messages"
        protected val LINE_SEPARATOR = System.getProperty("line.separator")
    }
}