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
package org.ebics.client.user

import org.apache.xml.security.Init
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.exception.EbicsException
import org.ebics.client.interfaces.Configuration
import org.ebics.client.interfaces.PasswordCallback
import org.ebics.client.io.IOUtils
import org.ebics.client.messages.Messages
import org.ebics.client.user.base.EbicsUser
import org.ebics.client.user.serializable.Bank
import org.ebics.client.user.serializable.Partner
import org.ebics.client.user.serializable.User
import org.ebics.client.user.serializable.User.Companion.create
import org.ebics.client.user.serializable.User.Companion.saveCertificates
import org.ebics.client.utils.Constants
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URL
import java.security.GeneralSecurityException
import java.security.Security
import java.util.*
import java.util.stream.Collectors

/**
 * The EBICS Model is used to load/store information
 * about EBICS users/partners/banks from/to filesystem (Java Object Serialization)
 */
class EbicsModel(val configuration: Configuration) {

    companion object {
        private val logger = LoggerFactory.getLogger(EbicsModel::class.java)

        init {
            Init.init()
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun createSession(user: User, product: Product): EbicsSession {
        return EbicsSession(user, configuration, product)
    }

    /**
     * Creates the user necessary directories
     *
     * @param user the concerned user
     */
    private fun createUserDirectories(user: EbicsUser) {
        logger.info(
            Messages.getString(
                "user.create.directories", Constants.APPLICATION_BUNDLE_NAME,
                user.userInfo.userId
            )
        )
        IOUtils.createDirectories(configuration.getUserDirectory(user))
        IOUtils.createDirectories(configuration.getTransferTraceDirectory(user))
        IOUtils.createDirectories(configuration.getKeystoreDirectory(user))
        IOUtils.createDirectories(configuration.getLettersDirectory(user))
    }

    /**
     * Creates a new EBICS bank with the data you should have obtained from the
     * bank.
     *
     * @param url            the bank URL
     * @param url            the bank name
     * @param hostId         the bank host ID
     * @param useCertificate does the bank use certificates ?
     * @return the created ebics bank
     */
    private fun createBank(url: URL, name: String, hostId: String, useCertificate: Boolean): Bank {
        return Bank(url, name, hostId, useCertificate)
    }

    /**
     * Creates a new ebics partner
     *
     * @param bank      the bank
     * @param partnerId the partner ID
     */
    private fun createPartner(bank: Bank, partnerId: String): Partner {
        return Partner(bank, partnerId)
    }

    /**
     * Creates a new ebics user and generates its certificates.
     *
     * @param url              the bank url
     * @param bankName         the bank name
     * @param hostId           the bank host ID
     * @param partnerId        the partner ID
     * @param userId           UserId as obtained from the bank.
     * @param name             the user name,
     * @param email            the user email
     * @param country          the user country
     * @param organization     the user organization or company
     * @param useCertificates  does the bank use certificates ?
     * @param saveCertificates save generated certificates?
     * @param passwordCallback a callback-handler that supplies us with the password. This
     * parameter can be null, in this case no password is used.
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun createUser(
        url: URL, ebicsVersion: EbicsVersion, bankName: String, hostId: String, partnerId: String,
        userId: String, name: String, email: String?, country: String?, organization: String?,
        useCertificates: Boolean, saveCertificates: Boolean, passwordCallback: PasswordCallback?
    ): User {
        logger.info(Messages.getString("user.create.info", Constants.APPLICATION_BUNDLE_NAME, userId))
        val bank = createBank(url, bankName, hostId, useCertificates)
        val partner = createPartner(bank, partnerId)
        return try {
            val user = create(
                EbicsUserInfo(ebicsVersion, userId, name, email, country, organization, EbicsUserStatus()),
                partner
            )
            createUserDirectories(user)
            if (saveCertificates) {
                saveCertificates(user.userInfo, configuration.getKeystoreDirectory(user), passwordCallback!!)
            }
            configuration.serializationManager.serialize(bank)
            configuration.serializationManager.serialize(partner)
            configuration.serializationManager.serialize(user)
            createLetters(user)
            logger.info(
                Messages.getString("user.create.success", Constants.APPLICATION_BUNDLE_NAME, userId)
            )
            user
        } catch (e: Exception) {
            logger.error(
                Messages.getString("user.create.error", Constants.APPLICATION_BUNDLE_NAME), e
            )
            throw e
        }
    }

    @Throws(GeneralSecurityException::class, IOException::class, EbicsException::class, FileNotFoundException::class)
    fun createLetters(user: EbicsUser?) {
        val letters = with(configuration.letterManager) {
            listOf(
                createA005Letter(user),
                createE002Letter(user),
                createX002Letter(user)
            )
        }
        val directory = File(configuration.getLettersDirectory(user))
        for (letter in letters) {
            FileOutputStream(File(directory, letter.name)).use { out -> letter.writeTo(out) }
        }
    }

    /**
     * Loads a user knowing its ID
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun loadUser(hostId: String, partnerId: String, userId: String): User {
        logger.info(
            Messages.getString("user.load.info", Constants.APPLICATION_BUNDLE_NAME, userId)
        )
        return try {
            val bank: Bank = Bank.deserialize(configuration.serializationManager.getDeserializeStream("bank-$hostId"))
            val partner: Partner =
                Partner.deserialize(configuration.serializationManager.getDeserializeStream("partner-$partnerId"), bank)
            val user: User =
                User.deserialize(configuration.serializationManager.getDeserializeStream("user-$userId"), partner)

            logger.info(
                Messages.getString("user.load.success", Constants.APPLICATION_BUNDLE_NAME, userId)
            )
            user
        } catch (e: Exception) {
            logger.error(
                Messages.getString("user.load.error", Constants.APPLICATION_BUNDLE_NAME), e
            )
            throw e
        }
    }

    private fun listPersistentObjectId(prefix: String, extension: String): List<String> {
        val userFiles = File(configuration.serializationDirectory).list { dir: File?, name: String ->
            name.startsWith(prefix) && name.endsWith(
                ".$extension"
            )
        }
        return Arrays.stream(userFiles).map { name: String ->
            name.replaceFirst(prefix.toRegex(), "").replaceFirst("\\." + extension.toRegex(), "")
        }
            .collect(Collectors.toList())
    }

    fun listUserId(): List<String> {
        return listPersistentObjectId("user-", "cer")
    }

    fun listPartnerId(): List<String> {
        return listPersistentObjectId("partner-", "cer")
    }

    fun listBankId(): List<String> {
        return listPersistentObjectId("bank-", "cer")
    }

    /**
     * Serialize User object, its Partner object and its Bank object
     */
    fun saveUser(user: User, partner: Partner = user.partner, bank: Bank = partner.bank) {
        saveUser(user)
        savePartner(partner)
        saveBank(bank)
    }

    private fun saveBank(bank: Bank) {
        try {
            logger.info(
                Messages.getString(
                    "bank.save.info", Constants.APPLICATION_BUNDLE_NAME,
                    bank.hostId
                )
            )
            configuration.serializationManager.serialize(bank)
        } catch (e: EbicsException) {
            logger.info(
                Messages.getString("app.quit.error", Constants.APPLICATION_BUNDLE_NAME)
            )
        }
    }

    private fun savePartner(partner: Partner) {
        try {
            logger.info(
                Messages.getString(
                    "partner.save.info", Constants.APPLICATION_BUNDLE_NAME,
                    partner.partnerId
                )
            )
            configuration.serializationManager.serialize(partner)
        } catch (e: EbicsException) {
            logger.info(
                Messages.getString("app.quit.error", Constants.APPLICATION_BUNDLE_NAME)
            )
        }
    }

    private fun saveUser(user: User) {
        try {
            logger.info(
                Messages.getString(
                    "user.save.info", Constants.APPLICATION_BUNDLE_NAME,
                    user.userInfo.userId
                )
            )
            configuration.serializationManager.serialize(user)
        } catch (e: EbicsException) {
            logger.info(
                Messages.getString("app.quit.error", Constants.APPLICATION_BUNDLE_NAME)
            )
        }
    }

    fun clearTraces() {
        logger.info(
            Messages.getString("app.cache.clear", Constants.APPLICATION_BUNDLE_NAME)
        )
        configuration.traceManager.clear()
    }

    init {
        Messages.setLocale(configuration.locale)
        logger.info(
            Messages.getString("init.configuration", Constants.APPLICATION_BUNDLE_NAME)
        )
        configuration.init()
    }
}