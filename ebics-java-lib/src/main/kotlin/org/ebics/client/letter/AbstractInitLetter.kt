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

import org.ebics.client.api.InitLetter
import org.ebics.client.messages.Messages
import java.io.IOException
import java.io.OutputStream
import java.util.*

abstract class AbstractInitLetter(
    protected val locale: Locale,
    hostId: String,
    bankName: String,
    userId: String,
    username: String,
    partnerId: String,
    version: String,
    certTitle: String,
    certificate: ByteArray?,
    hashTitle: String,
    val hash: ByteArray
) : InitLetter {

    private val letter: Letter = Letter(
        title, hostId, bankName, userId, username, partnerId,
        version, certTitle, certificate, hashTitle, hash, locale
    )

    @Throws(IOException::class)
    override fun writeTo(output: OutputStream) {
        output.write(letter.getLetterBytes())
    }

    companion object {

        /**
         * Returns the value of the property key.
         * @param key the property key
         * @param bundleName the bundle name
         * @param locale the bundle locale
         * @return the property value
         */
        @JvmStatic
        protected fun getString(key: String, bundleName: String, locale: Locale): String =
            Messages.getString(key, bundleName, locale)

        @JvmStatic
        protected val BUNDLE_NAME = "org.ebics.client.letter.messages"
    }
}