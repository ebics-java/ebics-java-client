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
package org.ebics.client.xml.h005

import org.ebics.client.exception.EbicsException
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.EbicsMessage
import org.ebics.client.order.EbicsService
import org.ebics.client.order.h005.OrderType
import org.ebics.client.utils.equalXml
import org.ebics.schema.h005.AuthOrderInfoType
import org.ebics.schema.h005.HTDReponseOrderDataType
import org.ebics.schema.h005.HTDResponseOrderDataDocument
import org.ebics.schema.h005.UserPermissionType

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
            //Filter out permissions are BTU/BTF without service (usually used for account permissions)
            return userInfo.permissionArray.filter { !(it.service == null && (it.adminOrderType == "BTU" || it.adminOrderType == "BTF")) }
                .map { permission ->
                    //Lets find for each user permission the referred BTF/BTU type
                    permission to partnerInfo.orderInfoArray.find {
                        it.adminOrderType == permission.adminOrderType &&
                                ((it.service == null && permission.service == null) ||
                                        (it.service != null && permission.service != null && it.service.equalXml(
                                            permission.service
                                        )))
                    }
                }.map {
                    //Lets merge information of the user permission with partner info about order type
                    merge2OrderType(it.first, it.second)
                }
        }
    }

    private fun merge2OrderType(
        userPermissionType: UserPermissionType,
        authOrderInfoType: AuthOrderInfoType?
    ): OrderType {
        return OrderType(
            EbicsAdminOrderType.valueOf(userPermissionType.adminOrderType),
            userPermissionType.service?.let { st ->
                EbicsService(
                    st.serviceName,
                    st.serviceOption,
                    st.scope,
                    EnumUtil.toContainerType(st.container),
                    EbicsMessage(st.msgName.stringValue, st.msgName?.variant, st.msgName?.version, st.msgName?.format)
                )
            },
            authOrderInfoType?.description,
            EnumUtil.toAuthLevel(userPermissionType),
            authOrderInfoType?.numSigRequired
        )
    }

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