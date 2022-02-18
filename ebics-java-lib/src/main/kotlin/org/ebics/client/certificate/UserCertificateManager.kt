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

import java.io.*
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*

/**
 * Simple manager for EBICS certificates.
 *
 * @author hacheni
 */
class UserCertificateManager(
    override val a005Certificate: X509Certificate,
    override val x002Certificate: X509Certificate,
    override val e002Certificate: X509Certificate,
    override val a005PrivateKey: PrivateKey,
    override val x002PrivateKey: PrivateKey,
    override val e002PrivateKey: PrivateKey
) : org.ebics.client.api.UserCertificateManager {
    private enum class KeyType { A005, X002, E002, }
    private class CertKeyPair(val certificate: X509Certificate, val privateKey: PrivateKey)

    companion object {
        /**
         * Creates the certificates for the user
         *
         * @throws GeneralSecurityException
         * @throws IOException
         */
        @Throws(GeneralSecurityException::class, IOException::class)
        fun create(userDn: String): UserCertificateManager {
            try {
                val calendar: Calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION)
                val endDate = Date(calendar.timeInMillis)
                val a005pair = createCertificate(KeyType.A005, userDn, endDate)
                val x002pair = createCertificate(KeyType.X002, userDn, endDate)
                val e002pair = createCertificate(KeyType.E002, userDn, endDate)
                return UserCertificateManager(
                    a005pair.certificate,
                    x002pair.certificate,
                    e002pair.certificate,
                    a005pair.privateKey,
                    x002pair.privateKey,
                    e002pair.privateKey,
                )
            } catch (ex:Exception) {
                throw IllegalArgumentException("Cant create certificate for dn='$userDn' error: ${ex.message}", ex)
            }
        }

        @Throws(GeneralSecurityException::class, IOException::class)
        private fun createCertificate(
            keyType: KeyType,
            userDn: String,
            end: Date
        ): CertKeyPair {
            val keypair: KeyPair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE)
            val cert = when (keyType) {
                KeyType.A005 -> X509Generator.generateA005Certificate(keypair, userDn, Date(), end)
                KeyType.X002 -> X509Generator.generateX002Certificate(keypair, userDn, Date(), end)
                KeyType.E002 -> X509Generator.generateE002Certificate(keypair, userDn, Date(), end)
            }
            return CertKeyPair(cert, keypair.private)
        }

        fun load(path: String, password: String, userId: String) =
            load(FileInputStream(path), password, userId)

        /**
         * Loads user certificates from a given key store
         *
         * @param path        the key store path
         * @param password the password call back
         * @throws GeneralSecurityException
         * @throws IOException
         */
        @Throws(GeneralSecurityException::class, IOException::class)
        fun load(ins: InputStream, password: String, keyStoreAliasPrefix: String): UserCertificateManager {
            val manager = KeyStoreManager.load(ins, password)
            return UserCertificateManager(
                a005Certificate = manager.getCertificate("$keyStoreAliasPrefix-A005"),
                x002Certificate = manager.getCertificate("$keyStoreAliasPrefix-X002"),
                e002Certificate = manager.getCertificate("$keyStoreAliasPrefix-E002"),
                a005PrivateKey = manager.getPrivateKey("$keyStoreAliasPrefix-A005"),
                x002PrivateKey = manager.getPrivateKey("$keyStoreAliasPrefix-X002"),
                e002PrivateKey = manager.getPrivateKey("$keyStoreAliasPrefix-E002"),
            )
        }
    }

    /**
     * Saves the certificates in PKCS12 format
     *
     * @param path        the certificates path
     * @param password the password call back
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun save(path: String, password: String, userId: String) {
        save(FileOutputStream("$path/$userId.p12"), password, userId)
    }

    /**
     * Writes a the generated certificates into a PKCS12 key store.
     *
     * @param password the key store password
     * @param fos      the output stream
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun save(fos: OutputStream, password: String, keyStoreAliasPrefix: String) {
        with(KeyStoreManager.create(password)) {
            setKeyEntry("$keyStoreAliasPrefix-A005", a005PrivateKey, a005Certificate)
            setKeyEntry("$keyStoreAliasPrefix-X002", x002PrivateKey, x002Certificate)
            setKeyEntry("$keyStoreAliasPrefix-E002", e002PrivateKey, e002Certificate)
            save(fos)
        }
    }
}