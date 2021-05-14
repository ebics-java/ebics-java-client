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

import org.ebics.client.exception.EbicsException
import org.ebics.client.interfaces.Configuration
import org.ebics.client.user.base.EbicsUser
import java.io.IOException
import java.security.interfaces.RSAPublicKey

/**
 * Communication hub for EBICS.
 *
 * @author Hachani
 */

/**
 * Constructs a new ebics session
 * @param user the ebics user
 * @param configuration the ebics client configuration
 */
class EbicsSession(
    /**
     * The session user.
     */
    val user: EbicsUser,
    /**
     * The client application configuration.
     */
    val configuration: Configuration,
    /**
     * Sets the optional product identification that will be sent to the bank during each request.
     */
    val product: Product
) {
    /**
     * Adds a session parameter to use it in the transfer process.
     * @param key the parameter key
     * @param value the parameter value
     */
    fun addSessionParam(key: String, value: String) {
        parameters[key] = value
    }

    /**
     * Retrieves a session parameter using its key.
     * @param key the parameter key
     * @return the session parameter
     */
    fun getSessionParam(key: String?): String? {
        return if (key == null) {
            null
        } else parameters[key]
    }

    private val parameters: MutableMap<String, String> = HashMap()

    /**
     * Returns the banks encryption key.
     * The key will be fetched automatically form the bank if needed.
     * @return the banks encryption key.
     * @throws IOException Communication error during key retrieval.
     * @throws EbicsException Server error message generated during key retrieval.
     */
    @Throws(IOException::class, EbicsException::class)
    fun getBankE002Key(): RSAPublicKey? {
        return user.partner.bank.e002Key
    }

    /**
     * Returns the banks authentication key.
     * The key will be fetched automatically form the bank if needed.
     * @return the banks authentication key.
     * @throws IOException Communication error during key retrieval.
     * @throws EbicsException Server error message generated during key retrieval.
     */
    @Throws(IOException::class, EbicsException::class)
    fun getBankX002Key(): RSAPublicKey? {
        return user.partner.bank.x002Key
    }

    /**
     * Returns the bank id.
     * @return the bank id.
     * @throws EbicsException
     */
    @Throws(EbicsException::class)
    fun getBankID(): String {
        return user.partner.bank.hostId
    }
}