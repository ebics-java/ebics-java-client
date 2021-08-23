package org.ebics.client.order

class EbicsMessage (
    val messageName: String,
    val messageNameVariant: String?,
    val messageNameVersion: String?,
    val messageNameFormat: String?,
)