package org.ebics.client.api.trace.orderType

import org.ebics.client.order.IEbicsService
import org.ebics.client.order.h005.ContainerType
import javax.persistence.Embeddable
import javax.persistence.Embedded

@Embeddable
data class EbicsService(
    override val serviceName: String,
    override val serviceOption: String? = null,
    override val scope: String? = null,
    override val containerType: ContainerType? = null,

    @Embedded
    override val message: EbicsMessage
) : IEbicsService {
    companion object {
        fun fromEbicsService(ebicsService: IEbicsService) =
            EbicsService(
                ebicsService.serviceName, ebicsService.serviceOption, ebicsService.scope, ebicsService.containerType,
                EbicsMessage.fromEbicsMessage(ebicsService.message)
            )
    }
}
