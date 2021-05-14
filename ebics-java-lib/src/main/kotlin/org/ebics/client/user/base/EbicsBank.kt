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
package org.ebics.client.user.base

import java.io.Serializable
import java.net.URL
import java.security.interfaces.RSAPublicKey

/**
 * Details about EBICS communication with a given bank.
 *
 * @author Hachani
 */
interface EbicsBank : Serializable {
    /**
     * Returns the URL needed for communication to the bank.
     * @return the URL needed for communication to the bank.
     */
    val bankURL: URL

    /**
     *
     */
    val useCertificate: Boolean

    /**
     * Returns the encryption key digest you have obtained from the bank.
     * Ensure that nobody was able to modify the digest on its way from the bank to you.
     * @return the encryption key digest you have obtained from the bank.
     */
    val e002Digest: ByteArray?

    /**
     * Returns the authentication key digest you have obtained from the bank.
     * Ensure that nobody was able to modify the digest on its way from the bank to you.
     * @return the authentication key digest you have obtained from the bank.
     */
    val x002Digest: ByteArray?

    /**
     * Returns the banks encryption key.
     * The key will be fetched automatically form the bank if needed.
     * @return the banks encryption key.
     * @throws IOException Communication error during key retrieval.
     * @throws EbicsException Server error message generated during key retrieval.
     */
    /**
     * Returns the banks encryption key.
     * @return the banks encryption key.
     */
    val e002Key: RSAPublicKey?

    /**
     * Returns the banks authentication key.
     * The key will be fetched automatically form the bank if needed.
     * @return the banks authentication key.
     * @throws IOException Communication error during key retrieval.
     * @throws EbicsException Server error message generated during key retrieval.
     */
    /**
     * Returns the banks authentication key.
     * @return the banks authentication key.
     */
    val x002Key: RSAPublicKey?

    /**
     * Returns the bank id.
     * @return the bank id.
     * @throws EbicsException
     */
    /**
     * Returns the bank's id.
     * @return the bank's id.
     */
    val hostId: String

    /**
     * Returns the bank's name.
     * @return the bank's name.
     */
    val name: String

    /**
     * Keys have been fetched from the bank.
     * The getters for the appropriate attributes should return the given values from now on.
     * For the sake of performance the values should be persisted for later usage.
     *
     * @param e002Key the banks encryption key.
     * @param x002Key the banks authentication key.
     */
    fun setBankKeys(e002Key: RSAPublicKey, x002Key: RSAPublicKey)

    /**
     * Sets the bank digests.
     * @param e002Digest encryption digest
     * @param x002Digest authentication digest
     */
    fun setDigests(e002Digest: ByteArray, x002Digest: ByteArray)
}