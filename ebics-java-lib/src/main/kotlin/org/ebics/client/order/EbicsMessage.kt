package org.ebics.client.order

data class EbicsMessage (
    val messageName: String,
    val messageNameVariant: String?,
    val messageNameVersion: String?,
    val messageNameFormat: String?,
)