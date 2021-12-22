package org.ebics.client.api.trace.orderType

import org.ebics.client.order.h005.ContainerType
import javax.persistence.Embeddable
import javax.persistence.Embedded

@Embeddable
data class EbicsService(
    val serviceName: String,
    val serviceOption: String? = null,
    val scope: String? = null,
    val containerType: ContainerType? = null,

    @Embedded
    val message: EbicsMessage
)
