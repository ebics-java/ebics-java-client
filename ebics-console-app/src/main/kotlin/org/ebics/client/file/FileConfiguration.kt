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

import org.ebics.client.api.Configuration
import org.ebics.client.api.EbicsUser
import org.ebics.client.interfaces.LetterManager
import org.ebics.client.api.TraceManager
import org.ebics.client.io.IOUtils
import org.ebics.client.letter.DefaultLetterManager
import java.io.File
import java.util.*

/**
 * A simple client application configuration.
 *
 * @author hachani
 */
open class FileConfiguration constructor(
    /**
     * Creates a new application configuration.
     * The root directory will be user.home/ebics/client
     */
    private val rootDirectory: String = System.getProperty("user.home") + File.separator + "ebics" + File.separator + "client",
    override val httpProxyHost: String? = null,
    override val httpProxyPort: Int? = null,
    override val httpProxyUser: String? = null,
    override val httpProxyPassword: String? = null,

    ) : Configuration
{

    private val bundle: ResourceBundle = ResourceBundle.getBundle(RESOURCE_DIR)
    final override val isTraceEnabled: Boolean = true
    override val traceManager: TraceManager = FileTraceManager(isTraceEnabled)
    override val locale: Locale = Locale.ENGLISH
    override val letterManager: LetterManager = DefaultLetterManager(locale)
    override val sslTrustedStoreFile: String = rootDirectory + File.separator + getString("ssltruststore.file.name")
    private val usersDirectory: String = rootDirectory + File.separator + getString("users.dir.name")
    override val signatureVersion: String = getString("signature.version")
    override val authenticationVersion: String = getString("authentication.version")
    override val encryptionVersion: String = getString("encryption.version")
    override val isCompressionEnabled: Boolean = true

    /**
     * Returns the corresponding property of the given key
     * @param key the property key
     * @return the property value.
     */
    fun getString(key: String): String {
        return try {
            bundle.getString(key)
        } catch (e: MissingResourceException) {
            "!!$key!!"
        }
    }

    override fun init() {
        //Create the root directory
        IOUtils.createDirectories(rootDirectory)
        //create the SSL trusted stores directories
        IOUtils.createDirectories(sslTrustedStoreFile)
        //Create users directory
        IOUtils.createDirectories(usersDirectory)
    }

    /**
     * Returns the directory path of the key store that contains
     * bank and user certificates.
     * @param user the ebics user.
     * @return the key store directory of a given user.
     */
    fun getKeystoreDirectory(user: EbicsUser): String {
        return getUserDirectory(user) + File.separator + getString("keystore.dir.name")
    }

    /**
     * Returns the directory path that contains the traces
     * XML transfer files.
     * @param user the ebics user
     * @return the transfer trace directory
     */
    fun getTransferTraceDirectory(user: EbicsUser): String {
        return getUserDirectory(user) + File.separator + getString("traces.dir.name")
    }

    /**
     * Returns the initializations letters directory.
     * @return the initializations letters directory.
     */
    fun getLettersDirectory(user: EbicsUser): String {
        return getUserDirectory(user) + File.separator + getString("letters.dir.name")
    }

    /**
     * Returns the users directory.
     * @return the users directory.
     */
    fun getUserDirectory(user: EbicsUser): String {
        return usersDirectory + File.separator + user.userInfo.userId
    }

    companion object {
        private const val RESOURCE_DIR = "org.ebics.client.console.default-config"
    }
}