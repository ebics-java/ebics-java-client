package org.ebics.client.order.h004

import org.ebics.client.order.AbstractEbicsUploadOrderResponse

class EbicsUploadOrderResponse(orderNumber: String, transactionId: ByteArray) :
    AbstractEbicsUploadOrderResponse(orderNumber, transactionId)