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

import org.ebics.client.interfaces.ContentFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Byte array content factory that delivers the file content
 * as a `ByteArrayInputStream`. This object is
 * serializable in a way to recover interrupted file transfers.
 *
 * @author hachani
 */
/**
 * Constructs a new `ByteArrayContentFactory` with
 * a given byte array content.
 * @param content the byte array content
 */
class ByteArrayContentFactory
(
    private val byteContent: ByteArray
) : ContentFactory {
    override val content: InputStream
        get() = ByteArrayInputStream(byteContent)
}