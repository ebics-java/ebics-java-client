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

import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserAction
import org.ebics.client.model.user.EbicsUserStatus
import org.ebics.client.model.user.EbicsUserStatusEnum

/**
 * Basic EBICS User data fields, without keys & relations to partners/bank
 *
 * @author Hachani
 * @author JTO
 */
interface EbicsUserInfo {
    val ebicsVersion: EbicsVersion
    val userId: String
    val name: String
    val dn: String
    var userStatus: EbicsUserStatusEnum
    val securityMedium: String get() = "0000"
    /**
     * Does the user use certificates (or just public key exponent & modulus)
     * Can be used for H003, H004 only
     * For H005 is always true per EBICS standard
     */
    val useCertificate: Boolean

    fun checkAction(action: EbicsUserAction) = userStatus.checkAction(action)
    fun updateStatus(action: EbicsUserAction) { userStatus = userStatus.updateStatus(action) }
}