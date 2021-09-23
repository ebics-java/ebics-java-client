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
package org.ebics.client.io

import org.ebics.client.api.UserCertificateManager
import org.ebics.client.exception.EbicsException
import org.ebics.client.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.GeneralSecurityException

/**
 * A simple mean to join downloaded segments from the
 * bank ebics server.
 *
 * @author Hachani
 */
class Joiner(
    private val userCert: UserCertificateManager
) {
    @Throws(EbicsException::class)
    fun append(data: ByteArray) {
        try {
            buffer.write(data)
            buffer.flush()
        } catch (e: IOException) {
            throw EbicsException(e.message)
        }
    }

    /**
     * Writes the joined part to an output stream.
     * @param output the output stream.
     * @param transactionKey the transaction key
     * @throws EbicsException
     */
    @Throws(EbicsException::class)
    fun writeTo(output: OutputStream, transactionKey: ByteArray) {
        try {
            buffer.close()
            val decrypted: ByteArray = userCert.decrypt(buffer.toByteArray(), transactionKey)
            output.write(Utils.unzip(decrypted))
        } catch (e: GeneralSecurityException) {
            throw EbicsException(e.message)
        } catch (e: IOException) {
            throw EbicsException(e.message)
        }
    }

    private val buffer: ByteArrayOutputStream = ByteArrayOutputStream()
}