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

import org.ebics.client.exception.EbicsException
import org.ebics.client.exception.h005.EbicsReturnCode
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.model.EbicsVersion
import org.ebics.client.xml.h005.DefaultResponseElement
import org.ebics.schema.h000.EbicsHEVResponseDocument
import org.ebics.schema.h000.HEVResponseDataType


class HEVResponse(factory: ContentFactory) : DefaultResponseElement(factory) {

    @Throws(EbicsException::class)
    override fun build() {
        parse(factory)
        response = (document as EbicsHEVResponseDocument).ebicsHEVResponse
        returnCode = EbicsReturnCode.toReturnCode(response.systemReturnCode.returnCode, "")
    }

    fun getSupportedVersions(): List<EbicsVersion> = EnumUtil.toEbicsVersions(response)

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------
    lateinit var response: HEVResponseDataType

    companion object {
        private const val serialVersionUID = -1305363936881364049L
    }
}