package org.ebics.client.order

data class EbicsMessage (
    override val messageName: String,
    override val messageNameVariant: String?,
    override val messageNameVersion: String?,
    override val messageNameFormat: String?,
) : IEbicsMessage