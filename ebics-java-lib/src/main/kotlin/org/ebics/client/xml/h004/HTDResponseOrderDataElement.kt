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
package org.ebics.client.xml.h004

import org.ebics.client.exception.EbicsException
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.order.AuthorisationLevel
import org.ebics.client.order.h004.OrderType
import org.ebics.schema.h004.AuthOrderInfoType
import org.ebics.schema.h004.HTDReponseOrderDataType
import org.ebics.schema.h004.HTDResponseOrderDataDocument

/**
 * The `HPBResponseOrderDataElement` contains the public bank
 * keys in encrypted mode. The user should decrypt with his encryption
 * key to have the bank public keys.
 *
 * @author hachani
 */
/**
 * Creates a new `HPBResponseOrderDataElement` from a given
 * content factory.
 * @param factory the content factory.
 */
class HTDResponseOrderDataElement(factory: ContentFactory) : DefaultResponseElement(factory) {

    @Throws(EbicsException::class)
    override fun build() {
        parse(factory)
        response = (document as HTDResponseOrderDataDocument).htdResponseOrderData
    }

    /**
     * Return list of order-types available for user
     * with all details (description, number of signatures, rights,..)
     */
    fun getOrderTypes(): List<OrderType> {
        with(response) {
            return userInfo.permissionArray.flatMap { permissions ->
                //Lets find for each user permission the referred BTF type
                permissions.orderTypes.mapNotNull { orderType ->
                    partnerInfo.orderInfoArray.find { orderInfo -> orderInfo.orderType == orderType }
                        ?.let { it to EnumUtil.toAuthLevel(permissions)}
                }
            }.map {
                createOrderType(it.first, it.second)
            }
        }
    }


    private fun createOrderType(orderInfo: AuthOrderInfoType, authLevel: AuthorisationLevel?): OrderType =
        OrderType(
            EnumUtil.recognizeAdminOrderType(orderInfo),
            orderInfo.orderType,
            EnumUtil.toTransferType(orderInfo),
            orderInfo.description,
            authLevel,
            orderInfo.numSigRequired
        )

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------
    lateinit var response: HTDReponseOrderDataType

    companion object {
        private const val serialVersionUID = -1305363936881364049L

        private fun parseHtdAndGetOrderTypes(htdContent: ByteArray): List<OrderType> {
            return HTDResponseOrderDataElement(ByteArrayContentFactory(htdContent)).apply {
                build()
                validate()
            }.getOrderTypes()
        }

        /**
         * Shortcut to ordertypes from HTD xml, for further processing
         */
        fun getOrderTypes(htdContent: ByteArray): List<OrderType> = parseHtdAndGetOrderTypes(htdContent)
    }
}