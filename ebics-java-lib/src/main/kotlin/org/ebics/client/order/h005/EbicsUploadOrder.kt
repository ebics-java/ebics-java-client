package org.ebics.client.order.h005

import org.ebics.client.order.AbstractEbicsUploadOrder
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.EbicsService

/**
 * Initialize H005 upload order
 * @param ebicsService the ECBIS service for BTU
 * @param edsFlag the EDS flag (whether the signature is provided)
 * @param fileName the optional filename of uploaded file
 */
class EbicsUploadOrder
(
    val orderService: EbicsService,
    val signatureFlag: Boolean,
    val edsFlag: Boolean,
    val fileName: String,
    params: Map<String, String>
) : AbstractEbicsUploadOrder(EbicsAdminOrderType.BTU, params) {
    override fun toString(): String {
        return "BTU=[$orderService] signatureFlag=$signatureFlag, edsFlag=$edsFlag, fileName=$fileName"
    }
}