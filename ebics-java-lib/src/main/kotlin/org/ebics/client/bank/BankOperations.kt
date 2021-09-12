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
package org.ebics.client.bank

import org.ebics.client.api.Configuration
import org.ebics.client.exception.EbicsException
import org.ebics.client.http.HttpRequestSender

import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.model.EbicsVersion
import org.ebics.client.xml.h000.HEVRequest
import org.ebics.client.xml.h000.HEVResponse
import java.io.IOException

class BankOperations(val configuration: Configuration)  {
    @Throws(EbicsException::class, IOException::class)
    fun sendHEV(bankURL: String, bankHostId: String): List<EbicsVersion> {
        val sender = HttpRequestSender(configuration, bankURL)
        val request = HEVRequest(bankHostId).apply { build(); validate() }
        val responseBody = sender.send(ByteArrayContentFactory(request.prettyPrint()))
        val response =  HEVResponse(responseBody).apply {
            build()
            validate()
        }
        response.report()
        return response.getSupportedVersions()
    }
}