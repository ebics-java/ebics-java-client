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

import org.ebics.client.exception.EbicsException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.GeneralSecurityException

/**
 * The `InitLetter` is an abstract initialization
 * letter. The INI, HIA and HPB letters should be an implementation
 * of the `InitLetter`
 *
 * @author Hachani
 */
interface InitLetter {

    /**
     * Saves the `InitLetter` to the given output stream.
     * @param output the output stream.
     * @throws IOException Save error.
     */
    @Throws(IOException::class)
    fun writeTo(output: OutputStream)

    fun toStr(): String {
        ByteArrayOutputStream().use { out ->
            writeTo(out)
            return out.toString("UTF-8")
        }
    }

    /**
     * Returns the initialization letter title.
     * @return the letter title.
     */
    val title: String

    /**
     * Returns the letter name.
     * @return the letter name.
     */
    val name: String
}