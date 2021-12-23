package org.ebics.client.order.h004

import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.IOrderTypeDefinition25

data class OrderTypeDefinition(
    override val adminOrderType: EbicsAdminOrderType,
    override val businessOrderType: String? = null
) : IOrderTypeDefinition25
