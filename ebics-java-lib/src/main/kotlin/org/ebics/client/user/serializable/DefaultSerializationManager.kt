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

import org.ebics.client.exception.EbicsException
import org.ebics.client.io.IOUtils
import java.io.*

/**
 * A simple implementation of the `SerializationManager`.
 * The serialization process aims to save object on the user disk
 * using a separated file for each object to serialize.
 *
 * @author hachani
 */
class DefaultSerializationManager(
    private var serializationDir: File? = null
) : SerializationManager {

    @Throws(EbicsException::class)
    override fun serialize(obj: Serializable) {
        try {
            val out = ObjectOutputStream(FileOutputStream(IOUtils.createFile(serializationDir, obj.saveName)))
            obj.serialize(out)
        } catch (e: IOException) {
            throw EbicsException(e.message)
        }
    }

    @Throws(EbicsException::class)
    override fun getDeserializeStream(name: String): ObjectInputStream {
        return try {
            ObjectInputStream(FileInputStream(IOUtils.createFile(serializationDir, "$name.cer")))
        } catch (e: IOException) {
            throw EbicsException(e.message)
        }
    }

    override fun setSerializationDirectory(serializationDir: String) {
        this.serializationDir = File(serializationDir)
    }
}