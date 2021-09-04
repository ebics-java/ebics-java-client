package org.ebics.client.order.h003

import org.ebics.client.order.AbstractEbicsUploadOrderResponse

class EbicsUploadOrderResponse(orderNumber: String, transactionId: ByteArray) :
    AbstractEbicsUploadOrderResponse(orderNumber, transactionId)