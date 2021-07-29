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
package org.ebics.client.file

import org.apache.xml.security.Init
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.exception.EbicsException

import org.ebics.client.io.IOUtils
import org.ebics.client.messages.Messages
import org.ebics.client.model.*
import org.ebics.client.api.EbicsUser
import org.ebics.client.certificate.BankCertificateManager
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.model.user.EbicsUserStatusEnum
import org.ebics.client.utils.Constants
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URL
import java.security.GeneralSecurityException
import java.security.Security

/**
 * The EBICS Model is used to load/store information
 * about EBICS users/partners/banks from/to filesystem (Java Object Serialization)
 */
class EbicsFileModel(
    private val configuration: FileConfiguration,
    serializationDirectory: String,
) {
    private val serializationManager: SerializationManager = FileSerializationManager(File(serializationDirectory))

    companion object {
        private val logger = LoggerFactory.getLogger(EbicsFileModel::class.java)

        init {
            Init.init()
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun createSession(
        user: User,
        product: Product,
        userCert: UserCertificateManager,
        bankCert: BankCertificateManager?
    ): EbicsSession {
        return EbicsSession(user, configuration, product, userCert, bankCert)
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
                user.userId
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
     * @param name           the bank name
     * @param hostId         the bank host ID
     * @return the created ebics bank
     */
    private fun createBank(url: URL, name: String, hostId: String): Bank = Bank(url, name, hostId)

    /**
     * Creates a new ebics partner
     *
     * @param bank      the bank
     * @param partnerId the partner ID
     */
    private fun createPartner(bank: Bank, partnerId: String): Partner = Partner(bank, partnerId)

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
     * @param password a callback-handler that supplies us with the password. This
     * parameter can be null, in this case no password is used.
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun createUser(
        url: URL, ebicsVersion: EbicsVersion, bankName: String, hostId: String, partnerId: String,
        userId: String, name: String, email: String?, country: String?, organization: String?,
        useCertificates: Boolean, saveCertificates: Boolean, password: String
    ): Pair<User, UserCertificateManager> {
        logger.info(Messages.getString("user.create.info", Constants.APPLICATION_BUNDLE_NAME, userId))
        val bank = createBank(url, bankName, hostId)
        val partner = createPartner(bank, partnerId)
        return try {
            val user = User(partner, ebicsVersion, userId, name, EbicsUser.makeDN(name, email, country, organization), EbicsUserStatusEnum.CREATED, useCertificates)
            val userCert = UserCertificateManager.create(user.dn)
            if (saveCertificates) {
                userCert.save(configuration.getKeystoreDirectory(user), password, userId)
            }
            createUserDirectories(user)
            serializationManager.serialize(bank)
            serializationManager.serialize(partner)
            serializationManager.serialize(user)
            createLetters(user, userCert)
            logger.info(
                Messages.getString("user.create.success", Constants.APPLICATION_BUNDLE_NAME, userId)
            )
            user to userCert
        } catch (e: Exception) {
            logger.error(
                Messages.getString("user.create.error", Constants.APPLICATION_BUNDLE_NAME), e
            )
            throw e
        }
    }

    @Throws(GeneralSecurityException::class, IOException::class, EbicsException::class, FileNotFoundException::class)
    fun createLetters(user: EbicsUser, userCert: UserCertificateManager) {
        val letters = with(configuration.letterManager) {
            listOf(
                createA005Letter(user, userCert),
                createE002Letter(user, userCert),
                createX002Letter(user, userCert)
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
    fun loadUser(
        hostId: String,
        partnerId: String,
        userId: String,
        password: String
    ): Pair<User, UserCertificateManager> {
        logger.info(
            Messages.getString("user.load.info", Constants.APPLICATION_BUNDLE_NAME, userId)
        )
        return try {
            val bank: Bank = Bank.deserialize(serializationManager.getDeserializeStream("bank-$hostId"))
            val partner: Partner =
                Partner.deserialize(serializationManager.getDeserializeStream("partner-$partnerId"), bank)
            val user: User =
                User.deserialize(serializationManager.getDeserializeStream("user-$userId"), partner)
            val manager =
                UserCertificateManager.load(configuration.getKeystoreDirectory(user), password, userId)
            logger.info(
                Messages.getString("user.load.success", Constants.APPLICATION_BUNDLE_NAME, userId)
            )
            user to manager
        } catch (e: Exception) {
            logger.error(
                Messages.getString("user.load.error", Constants.APPLICATION_BUNDLE_NAME), e
            )
            throw e
        }
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
            serializationManager.serialize(bank)
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
            serializationManager.serialize(partner)
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
                    user.userId
                )
            )
            serializationManager.serialize(user)
        } catch (e: EbicsException) {
            logger.info(
                Messages.getString("app.quit.error", Constants.APPLICATION_BUNDLE_NAME)
            )
        }
    }
}