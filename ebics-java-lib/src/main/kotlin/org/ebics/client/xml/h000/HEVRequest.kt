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
package org.ebics.client.xml.h000

import org.ebics.client.api.EbicsSession
import org.ebics.client.exception.EbicsException
import org.ebics.client.xml.h005.DefaultEbicsRootElement
import org.ebics.schema.h000.EbicsHEVRequestDocument

/**
 * The HEV request
 *
 * @author Jan Toegel
 */

class HEVRequest(val ebicsHostID: String) : DefaultEbicsRootElement() {
    @Throws(EbicsException::class)
    override fun build() {
        with (EbicsHEVRequestDocument.Factory.newInstance()) {
            this.addNewEbicsHEVRequest().hostID = ebicsHostID
            document = this
        }
    }

    override fun toByteArray(): ByteArray {
        setSaveSuggestedPrefixes("http://www.ebics.org/h000", "")
        return super.toByteArray()
    }

    companion object {
        private const val serialVersionUID = -5523105558015982970L
    }
}