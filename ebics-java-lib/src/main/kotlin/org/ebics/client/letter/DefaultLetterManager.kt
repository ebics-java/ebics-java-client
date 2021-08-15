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
package org.ebics.client.letter

import org.ebics.client.api.EbicsUser
import org.ebics.client.api.InitLetter
import org.ebics.client.exception.EbicsException
import org.ebics.client.api.LetterManager
import org.ebics.client.api.UserCertificateManager
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.*

/**
 * The `DefaultLetterManager` is a simple way
 * to manage initialization letters.
 *
 * @author Hachani
 */
class DefaultLetterManager(private val locale: Locale) : LetterManager {
    @Throws(GeneralSecurityException::class, IOException::class, EbicsException::class)
    override fun createA005Letter(user: EbicsUser, userCert: UserCertificateManager): AbstractInitLetter =
        A005Letter(locale, user, userCert)

    @Throws(GeneralSecurityException::class, IOException::class, EbicsException::class)
    override fun createE002Letter(user: EbicsUser, userCert: UserCertificateManager): AbstractInitLetter =
        E002Letter(locale, user, userCert)

    @Throws(GeneralSecurityException::class, IOException::class, EbicsException::class)
    override fun createX002Letter(user: EbicsUser, userCert: UserCertificateManager): AbstractInitLetter =
        X002Letter(locale, user, userCert)
}