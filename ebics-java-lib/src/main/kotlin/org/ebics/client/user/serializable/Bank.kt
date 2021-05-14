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
package org.ebics.client.user.serializable

import org.ebics.client.user.base.EbicsBank
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
    /**
     * Does the bank use certificates for signing/crypting ?
     * @serial
     */
    override val useCertificate: Boolean
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

    override fun setBankKeys(e002Key: RSAPublicKey, x002Key: RSAPublicKey) {
        this.e002Key = e002Key
        this.x002Key = x002Key
    }

    override fun setDigests(e002Digest: ByteArray, x002Digest: ByteArray) {
        this.e002Digest = e002Digest
        this.x002Digest = x002Digest
    }

    override val saveName: String = "$savePrefix$hostId.cer"
    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    /**
     * The bank encryption digest
     * @serial
     */
    override var e002Digest: ByteArray? = null
        private set

    /**
     * The bank authentication digest
     * @serial
     */
    override var x002Digest: ByteArray? = null
        private set

    /**
     * The ban encryption key
     * @serial
     */
    override var e002Key: RSAPublicKey? = null
        private set

    /**
     * The ban encryption key
     * @serial
     */
    override var x002Key: RSAPublicKey? = null
        private set

    companion object {

        /**
         * deserialize is called to restore the state of the Bank from the stream.
         * @param ois the object input stream
         * @throws IOException
         * @throws ClassNotFoundException
         */
        @JvmStatic
        @Throws(IOException::class, ClassNotFoundException::class)
        fun deserialize(ois: ObjectInputStream):Bank =
            ois.readObject() as Bank

        val savePrefix: String
            get() = "bank-"
        private const val serialVersionUID = 2123071449956793284L
    }
}