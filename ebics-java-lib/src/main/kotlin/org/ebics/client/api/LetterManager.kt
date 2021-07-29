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
package org.ebics.client.api

import org.ebics.client.exception.EbicsException
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Initialization letters manager.
 * Manages the INI, HIA and the HPB letters.
 *
 * @author Hachani
 */
interface LetterManager {
    /**
     * Creates the initialization letter for the INI request.
     * This letter contains information about the signature certificate
     * of the given user.
     * @param user the ebics user.
     * @return the INI letter.
     * @throws EbicsException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class, IOException::class, EbicsException::class)
    fun createA005Letter(user: EbicsUser, userCert:UserCertificateManager): InitLetter

    /**
     * Creates the initialization letter for the HIA request.
     * This letter contains information about the encryption
     * certificates of the given user.
     * @param user the ebics user
     * @return the HIA letter
     * @throws EbicsException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class, IOException::class, EbicsException::class)
    fun createE002Letter(user: EbicsUser, userCert:UserCertificateManager): InitLetter

    /**
     * Creates the initialization letter for the HIA request.
     * This letter contains information about the authentication
     * certificates of the given user.
     * @param user the ebics user
     * @return the HIA letter
     * @throws EbicsException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class, IOException::class, EbicsException::class)
    fun createX002Letter(user: EbicsUser, userCert:UserCertificateManager): InitLetter
}