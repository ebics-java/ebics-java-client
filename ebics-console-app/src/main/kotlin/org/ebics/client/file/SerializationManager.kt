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

import org.ebics.client.api.Serializable
import org.ebics.client.exception.EbicsException
import java.io.ObjectInputStream

/**
 * A mean to serialize and deserialize `Object`.
 * The manager should ensure serialization and deserialization
 * operations
 *
 * @author hachani
 */
interface SerializationManager {
    /**
     * Serializes a `Savable` object
     * @param obj the `Savable` object$
     * @throws EbicsException serialization fails
     */
    @Throws(EbicsException::class)
    fun serialize(obj: Serializable)

    /**
     * Deserializes the given object input stream.
     * @param name the name of the serialized object
     * @return the corresponding object input stream
     * @throws EbicsException deserialization fails
     */
    @Throws(EbicsException::class)
    fun getDeserializeStream(name: String): ObjectInputStream
}