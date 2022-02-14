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

import org.apache.commons.codec.binary.Base64
import org.ebics.client.api.EbicsUser
import org.ebics.client.api.UserCertificateManager
import org.ebics.client.certificate.KeyUtil
import java.util.*

/**
 * The `X002Letter` is the initialization letter
 * for the authentication certificate.
 *
 * @author Hachani
 */
class X002Letter(
    locale: Locale,
    user: EbicsUser,
    userCert: UserCertificateManager,
    useCert: Boolean = user.useCertificate
) : AbstractInitLetter(
    locale,
    user.partner.bank.hostId,
    user.partner.bank.name,
    user.userId,
    user.name,
    user.partner.partnerId,
    getString("HIALetter.x002.version", BUNDLE_NAME, locale),
    getString("HIALetter.x002.certificate", BUNDLE_NAME, locale),
    if (useCert) Base64.encodeBase64(userCert.getX002CertificateBytes(), true) else null,
    getString("HIALetter.x002.digest", BUNDLE_NAME, locale),
    if (useCert) KeyUtil.getCertificateHash(userCert.getX002CertificateBytes()) else KeyUtil.getKeyHash(userCert.x002PublicKey)
) {
    override val title: String
        get() = getString("HIALetter.x002.title", BUNDLE_NAME, locale)
    override val name: String
        get() = getString("HIALetter.x002.name", BUNDLE_NAME, locale) + ".txt"
}