package org.ebics.client.order

import org.ebics.client.order.h005.ContainerType

data class EbicsService(
    override val serviceName: String,
    override val serviceOption: String?,
    override val scope: String?,
    override val containerType: ContainerType?,
    override val message: IEbicsMessage
) : IEbicsService