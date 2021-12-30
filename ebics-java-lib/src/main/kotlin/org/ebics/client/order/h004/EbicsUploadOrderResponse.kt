package org.ebics.client.order.h004

import org.ebics.client.order.AbstractEbicsUploadOrderResponse

class EbicsUploadOrderResponse(orderNumber: String, transactionId: String) :
    AbstractEbicsUploadOrderResponse(orderNumber, transactionId)