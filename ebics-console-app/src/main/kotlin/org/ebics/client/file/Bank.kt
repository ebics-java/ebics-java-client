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
package org.ebics.client.file

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.Serializable
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.URL
import java.security.interfaces.RSAPublicKey

/**
 * Simple implementation of an EBICS bank.
 * This object is serializable to facilitate persisting of the values.
 * Save the the object whenever it needs to be saved.
 *
 * @author Hachani
 */
class Bank
/**
 * Constructs a new EBICS bank with the data you should have obtained from the bank.
 * @param url the bank URL
 * @param name the bank name
 * @param hostId the bank host ID
 * @param useCertificate does the bank use certificates for exchange ?
 */(
    /**
     * The bank URL
     * @serial
     */
    override val bankURL: URL,
    /**
     * The bank name
     * @serial
     */
    override val name: String,
    /**
     * The bank host id
     * @serial
     */
    override val hostId: String,

) : EbicsBank, Serializable {
    @Throws(IOException::class)
    override fun serialize(oos: ObjectOutputStream) {
        oos.writeObject(this)
        oos.flush()
        oos.close()
    }


    /**
     * WriteObject is called to save the state of the EbicsBank to an
     * ObjectOutputStream.
     *
     * @serialData the default write object.
     * throw an IOException if it does not.
     */
    @Throws(IOException::class)
    private fun writeObject(oos: ObjectOutputStream) {
        oos.defaultWriteObject() // write the fields
    }

    override val saveName: String = "$savePrefix$hostId.cer"

    companion object {

        /**
         * deserialize is called to restore the state of the Bank from the stream.
         * @param ois the object input stream
         * @throws IOException
         * @throws ClassNotFoundException
         */
        @JvmStatic
        @Throws(IOException::class, ClassNotFoundException::class)
        fun deserialize(ois: ObjectInputStream): Bank =
            ois.readObject() as Bank

        val savePrefix: String
            get() = "bank-"
        private const val serialVersionUID = 2123071449956793284L
    }
}