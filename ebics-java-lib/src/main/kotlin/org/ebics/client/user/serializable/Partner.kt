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

import org.ebics.client.user.base.EbicsPartner
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Simple implementation of an EBICS customer.
 * This object is not serializable, but it should be persisted every time it needs to be saved.
 * Persistence is achieved via `save(ObjectOutputStream)` and the matching constructor.
 *
 * @author Hachani
 */
class Partner(
    /**
     * First time constructor.
     * @param bank the bank
     * @param partnerId the partner ID
     */
    override val bank: Bank,
    override val partnerId: String,
    /**
     * Returns the next order available ID
     * @return the next order ID
     */
    override var orderId: Int = 10 * 36 * 36 * 36
) : EbicsPartner, Serializable {

    companion object {
        /**
         * Reconstructs a persisted EBICS customer.
         * @param bank the bank
         * @param ois the stream object
         * @throws IOException
         */
        @JvmStatic
        fun deserialize(ois: ObjectInputStream, bank: Bank): Partner =
            Partner(bank, ois.readUTF(), ois.readInt())
    }

    @Throws(IOException::class)
    override fun serialize(oos: ObjectOutputStream) {
        oos.writeUTF(partnerId)
        oos.writeInt(orderId)
        oos.flush()
        oos.close()
    }

    override val saveName: String = "partner-$partnerId.cer"
}