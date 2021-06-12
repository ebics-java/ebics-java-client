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
import org.ebics.client.interfaces.PasswordCallback
import java.io.*
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.KeyStore
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
            val calendar: Calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION)
            val generator = X509Generator()
            val endDate = Date(calendar.timeInMillis)
            val a005pair = createCertificate(KeyType.A005, generator, userDn, endDate)
            val x002pair = createCertificate(KeyType.X002, generator, userDn, endDate)
            val e002pair = createCertificate(KeyType.E002, generator, userDn, endDate)
            return UserCertificateManager(
                a005pair.certificate,
                x002pair.certificate,
                e002pair.certificate,
                a005pair.privateKey,
                x002pair.privateKey,
                e002pair.privateKey,
            )
        }

        @Throws(GeneralSecurityException::class, IOException::class)
        private fun createCertificate(
            keyType: KeyType,
            generator: X509Generator,
            userDn: String,
            end: Date
        ): CertKeyPair {
            val keypair: KeyPair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE)
            val cert = when (keyType) {
                KeyType.A005 -> generator.generateA005Certificate(keypair, userDn, Date(), end)
                KeyType.X002 -> generator.generateX002Certificate(keypair, userDn, Date(), end)
                KeyType.E002 -> generator.generateE002Certificate(keypair, userDn, Date(), end)
            }
            return CertKeyPair(cert, keypair.private)
        }

        fun load(path: String, passwordCallback: PasswordCallback, userId: String) =
            load(FileInputStream(path), passwordCallback, userId)

        /**
         * Loads user certificates from a given key store
         *
         * @param path        the key store path
         * @param passwordCallback the password call back
         * @throws GeneralSecurityException
         * @throws IOException
         */
        @Throws(GeneralSecurityException::class, IOException::class)
        fun load(ins: InputStream, passwordCallback: PasswordCallback, userId: String): UserCertificateManager {
            val manager = KeyStoreManager.load(ins, passwordCallback.password)
            return UserCertificateManager(
                a005Certificate = manager.getCertificate("$userId-A005"),
                x002Certificate = manager.getCertificate("$userId-X002"),
                e002Certificate = manager.getCertificate("$userId-E002"),
                a005PrivateKey = manager.getPrivateKey("$userId-A005"),
                x002PrivateKey = manager.getPrivateKey("$userId-X002"),
                e002PrivateKey = manager.getPrivateKey("$userId-E002"),
            )
        }
    }

    /**
     * Saves the certificates in PKCS12 format
     *
     * @param path        the certificates path
     * @param passwordCallback the password call back
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun save(path: String, passwordCallback: PasswordCallback, userId: String) {
        save(FileOutputStream("$path/$userId.p12"), passwordCallback, userId)
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
    fun save(fos: OutputStream, passwordCallback: PasswordCallback, userId: String) {
        val password = passwordCallback.password
        with(KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME)) {
            load(null, null)
            setKeyEntry("$userId-A005", a005PrivateKey, password, arrayOf(a005Certificate))
            setKeyEntry("$userId-X002", x002PrivateKey, password, arrayOf(x002Certificate))
            setKeyEntry("$userId-E002", e002PrivateKey, password, arrayOf(e002Certificate))
            store(fos, password)
        }
    }
}