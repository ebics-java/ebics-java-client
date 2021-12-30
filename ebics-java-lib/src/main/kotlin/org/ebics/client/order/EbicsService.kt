package org.ebics.client.order

import org.ebics.client.order.h005.ContainerType

data class EbicsService(
    override val serviceName: String,
    override val serviceOption: String?,
    override val scope: String?,
    override val containerType: ContainerType?,
    override val message: EbicsMessage
) : IEbicsService {
    override fun toString(): String {
        return listOf(
            serviceName,
            serviceOption,
            scope,
            containerType?.toString(),
            message.messageNameFormat,
            message.toString()
        ).map {  part -> part ?: '-' }.joinToString("|")
    }
}