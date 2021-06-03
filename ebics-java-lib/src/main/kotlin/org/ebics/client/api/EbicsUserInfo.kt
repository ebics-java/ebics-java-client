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
 * Things an EBICS user must be able to perform.
 *
 * @author Hachani
 */
interface EbicsUserInfo {
    val ebicsVersion: EbicsVersion
    val userId: String
    val name: String
    val dn: String
    var userStatus: EbicsUserStatusEnum

    fun checkAction(action: EbicsUserAction) = userStatus.checkAction(action)
    fun updateStatus(action: EbicsUserAction) { userStatus = userStatus.updateStatus(action) }

    companion object {

        /**
         * Makes the Distinguished Names for the user certificates.
         * @param name the user name
         * @param email the user email
         * @param country the user country
         * @param organization the user organization
         * @return
         */
        fun makeDN(
            name: String,
            email: String?,
            country: String?,
            organization: String?
        ): String {
            val buffer = StringBuilder()
            buffer.append("CN=").append(name)
            if (country != null) {
                buffer.append(", " + "C=").append(country.toUpperCase())
            }
            if (organization != null) {
                buffer.append(", " + "O=").append(organization)
            }
            if (email != null) {
                buffer.append(", " + "E=").append(email)
            }
            return buffer.toString()
        }
    }
}