package org.ebics.client.order

class EbicsService(
    val serviceName: String,
    val serviceOption: String,
    val scope: String,
    val containerType: String,
    val messageName: String,
    val messageNameVariant: String,
    val messageNameVersion: String,
    val messageNameFormat: String
)