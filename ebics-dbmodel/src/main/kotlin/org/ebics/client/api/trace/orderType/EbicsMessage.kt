package org.ebics.client.api.trace.orderType

import javax.persistence.Embeddable

@Embeddable
data class EbicsMessage (
    val messageName: String,
    val messageNameVariant: String? = null,
    val messageNameVersion: String? = null,
    val messageNameFormat: String? = null,
)