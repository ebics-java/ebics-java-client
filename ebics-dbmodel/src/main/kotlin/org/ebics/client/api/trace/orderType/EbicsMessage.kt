package org.ebics.client.api.trace.orderType

import org.ebics.client.order.IEbicsMessage
import javax.persistence.Embeddable

@Embeddable
data class EbicsMessage (
    override val messageName: String,
    override val messageNameVariant: String? = null,
    override val messageNameVersion: String? = null,
    override val messageNameFormat: String? = null,
) : IEbicsMessage {
    companion object {
        fun fromEbicsMessage(ebicsMessage: IEbicsMessage) =
            EbicsMessage(ebicsMessage.messageName, ebicsMessage.messageNameVariant, ebicsMessage.messageNameVersion, ebicsMessage.messageNameFormat)
    }
}