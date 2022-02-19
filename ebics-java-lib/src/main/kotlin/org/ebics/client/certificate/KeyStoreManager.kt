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
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.*
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.*


/**
 * Key store loader. This class loads a key store from
 * a given path and allow to get private keys and certificates
 * for a given alias.
 * The PKCS12 key store type is recommended to be used
 *
 * @author hachani
 */
class KeyStoreManager private constructor(
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
        fun load(ins: InputStream, password: String): KeyStoreManager =
            createKeyStoreManager(password).apply { load(ins) }

        /**
         * Creates a key store
         */
        @JvmStatic
        fun create(password: String) = createKeyStoreManager(password).apply { create() }

        private fun createKeyStoreManager(password: String) =
            KeyStoreManager(KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME), password)

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
    }

    /**
     * Creates empty keystore (Assuming)
     */
    private fun create() {
        keyStore.load(null, null)
    }

    fun setKeyEntry(alias: String, key: Key, certificate: X509Certificate) {
        keyStore.setKeyEntry(alias, key, password.toCharArray(), arrayOf(certificate))
    }

    /**
     * Saves the key store to a given output stream.
     * @param output the output stream.
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun save(output: OutputStream) {
        keyStore.store(output, password.toCharArray())
    }

    fun aliases(): Enumeration<String> = keyStore.aliases()
}