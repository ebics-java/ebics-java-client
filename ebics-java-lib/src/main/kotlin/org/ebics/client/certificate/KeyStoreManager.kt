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

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import sun.security.krb5.Confounder.bytes
import java.io.*
import java.math.BigInteger
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


/**
 * Key store loader. This class loads a key store from
 * a given path and allow to get private keys and certificates
 * for a given alias.
 * The PKCS12 key store type is recommended to be used
 *
 * @author hachani
 */
class KeyStoreManager private constructor(
    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------
    private val keyStore: KeyStore,
    private val password: String
) {
    /**
     * Loads a certificate for a given alias
     * @param alias the certificate alias
     * @return the certificate
     * @throws KeyStoreException
     */
    @Throws(KeyStoreException::class)
    fun getCertificate(alias: String): X509Certificate {
        val cert: Certificate? = keyStore.getCertificate(alias)
        requireNotNull(cert) { "alias $alias not found in the KeyStore" }
        return cert as X509Certificate
    }

    /**
     * Loads a private key for a given alias
     * @param alias the certificate alias
     * @return the private key
     * @throws GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class)
    fun getPrivateKey(alias: String): PrivateKey {
        val key: Key? = keyStore.getKey(alias, password.toCharArray())
        requireNotNull(key) { "private key not found for alias $alias" }
        return key as PrivateKey
    }

    companion object {
        /**
         * Loads a key store from a given path and password
         * @param path the key store path
         * @param password the key store password
         * @throws GeneralSecurityException
         * @throws IOException
         */
        @Throws(GeneralSecurityException::class, IOException::class)
        fun load(path: String, password: String): KeyStoreManager = load(FileInputStream(path), password)

        /**
         * Loads a key store from a given path and password
         * @param ins the InputStream with the keystore data
         * @param password the key store password
         * @throws GeneralSecurityException
         * @throws IOException
         */
        @Throws(GeneralSecurityException::class, IOException::class)
        fun load(ins: InputStream, password: String): KeyStoreManager = createKeyStoreManager(password).apply { load(ins) }

        /**
         * Creates a key store
         */
        @JvmStatic
        fun create(password: String) = createKeyStoreManager(password).apply { create() }

        private fun createKeyStoreManager(password: String) =
            KeyStoreManager(KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME), password)

        /**
         * Reads a certificate from an input stream for a given provider
         * @param input the input stream
         * @return the certificate
         * @throws CertificateException
         * @throws IOException
         */
        @Throws(CertificateException::class, IOException::class)
        fun readCertificate(input: InputStream): X509Certificate = readCertificate(input, Security.getProvider(BouncyCastleProvider.PROVIDER_NAME))


        /**
         * Reads a certificate from an input stream for a given provider
         * @param input the input stream
         * @param provider the certificate provider
         * @return the certificate
         * @throws CertificateException
         * @throws IOException
         */
        @Throws(CertificateException::class, IOException::class)
        fun readCertificate(input: InputStream, provider: Provider): X509Certificate {
            val certificate = CertificateFactory.getInstance("X.509", provider).generateCertificate(input)
            return if (certificate == null) {
                PEMParser(InputStreamReader(input)).readObject() as X509Certificate
            } else {
                certificate as X509Certificate
            }
        }

        @JvmStatic
        fun getPublicKey(publicExponent: BigInteger, modulus: BigInteger): RSAPublicKey {
            return try {
                KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(modulus, publicExponent)) as RSAPublicKey
            } catch (ex: Exception) {
                Logger.getLogger(KeyStoreManager::class.java.name).log(Level.SEVERE, "Exception creating bank public key from exponent & modulus", ex)
                throw ex
            }
        }

        /**
         * Reconstruct the RSAPublicKey from its encoded version (RSAPublicKey.getEncoded())
         */
        @JvmStatic
        fun getPublicKey(pubKeyEncoded: ByteArray): RSAPublicKey {
            return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(pubKeyEncoded)) as RSAPublicKey
        }
    }


    /**
     * Loads a key store from input stream
     * @param ins the input stream of key store.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    private fun load(ins: InputStream) {
        keyStore.load(ins, password.toCharArray())
        readCertificates()
    }

    /**
     * Creates empty keystore (Assuming)
     */
    private fun create() {
        keyStore.load(null, null)
    }

    /**
     * Returns the public key of a given certificate.
     * @param input the given certificate
     * @return The RSA public key of the given certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun getPublicKey(input: InputStream): RSAPublicKey {
        val cert: X509Certificate = readCertificate(input, keyStore.provider)
        return cert.publicKey as RSAPublicKey
    }

    /**
     * Writes the given certificate into the key store.
     * @param alias the certificate alias
     * @param input the given certificate.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun setCertificateEntry(alias: String, input: InputStream) {
        keyStore.setCertificateEntry(alias, readCertificate(input, keyStore.provider))
    }

    fun setCertificateEntry(alias: String, certificate: X509Certificate) {
        keyStore.setCertificateEntry(alias, certificate)
    }

    /**
     * Saves the key store to a given output stream.
     * @param output the output stream.
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun save(output: OutputStream) {
        keyStore.store(output, password.toCharArray())
    }

    /**
     * Reads all certificate existing in a given key store
     * @return A `Map` of certificate,
     * the key of the map is the certificate alias
     * @throws KeyStoreException
     */
    @Throws(KeyStoreException::class)
    private fun readCertificates() {
        val enumeration: Enumeration<String> = keyStore.aliases()
        while (enumeration.hasMoreElements()) {
            val alias: String = enumeration.nextElement()
            (certificates as MutableMap)[alias] = keyStore.getCertificate(alias) as X509Certificate
        }
    }

    /**
     * Returns the certificates contained in the key store.
     * @return the certificates contained in the key store.
     */
    private val certificates: Map<String, X509Certificate> = mutableMapOf()
}