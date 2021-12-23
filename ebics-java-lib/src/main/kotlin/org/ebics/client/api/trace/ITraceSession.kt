package org.ebics.client.api.trace

import org.ebics.client.api.EbicsSession
import org.ebics.client.interfaces.EbicsRootElement
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.IOrderTypeDefinition

interface ITraceSession {
    val session: EbicsSession
    val orderNumber: String
    val orderType: IOrderTypeDefinition
    val upload: Boolean
    val ebicsVersion: EbicsVersion
    fun trace(element: EbicsRootElement)
}

