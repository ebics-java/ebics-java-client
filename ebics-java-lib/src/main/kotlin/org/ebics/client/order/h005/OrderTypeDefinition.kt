package org.ebics.client.order.h005

import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.IEbicsService
import org.ebics.client.order.IOrderTypeDefinition30

data class OrderTypeDefinition(
    override val adminOrderType: EbicsAdminOrderType,
    override val service: IEbicsService? = null
) : IOrderTypeDefinition30
