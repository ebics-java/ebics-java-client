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
package org.ebics.client.model

import org.ebics.client.api.EbicsConfiguration
import org.ebics.client.api.EbicsSession
import org.ebics.client.api.EbicsUser
import org.ebics.client.certificate.BankCertificateManager
import org.ebics.client.certificate.UserCertificateManager
import java.util.*

/**
 * The EBICS Session for EBICS API requests
 * Used for both initialized and uninitialized EBICS connections (see @param bankCert)
 *
 * @author Hachani
 * @author honza.toegel
 */

class EbicsSession(
    /**
     * The session user.
     */
    override val user: EbicsUser,
    /**
     * The client application configuration.
     */
    override val configuration: EbicsConfiguration,
    /**
     * Sets the optional product identification that will be sent to the bank during each request.
     */
    override val product: Product,

    /**
     * User key-pairs (A005, X002, E002)
     */
    override val userCert: UserCertificateManager,

    /**
     * Bank public keys (X002, E002)
     * This is null for INI,HIA,HPB
     * Result of HPB request is BankCertificateManager,
     *  which is then set here as member, in order to use
     *  this for further operations like uploads / downloads / SPR
     */
    override val bankCert: BankCertificateManager?,

    /**
     * Unique sessionId
     */
    override val sessionId: String = UUID.randomUUID().toString(),
): EbicsSession {
    override val parameters: MutableMap<String, String> = HashMap()
}