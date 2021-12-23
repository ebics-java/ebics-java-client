package org.ebics.client.order

interface IEbicsMessage {
    val messageName: String
    val messageNameVariant: String?
    val messageNameVersion: String?
    val messageNameFormat: String?
}