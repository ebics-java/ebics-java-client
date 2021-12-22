package org.ebics.client.api.trace.orderType

import org.ebics.client.order.EbicsAdminOrderType
import javax.persistence.Embeddable
import javax.persistence.Embedded

@Embeddable
data class OrderTypeDefinition(
    //For H002-H005
    val adminOrderType: EbicsAdminOrderType,

    //For H005 order types
    @Embedded
    val ebicsServiceType: EbicsService? = null,
    //For H002-H004 order types
    val businessOrderType: String? = null,
)
