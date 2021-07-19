package org.ebics.client.ebicsrestapi.user.h005

import org.ebics.client.order.h005.EbicsUploadOrder

data class UploadRequest(val password:String,
    val ebicsUploadOrder: EbicsUploadOrder)
