package org.ebics.client.order.h004

import org.ebics.client.order.AbstractEbicsUploadOrder
import org.ebics.client.order.AttributeType
import org.ebics.client.order.EbicsAdminOrderType

/**
 * Initialize DE/FR H003, H004 upload order
 * @param orderType the order type for DE EBICS, for FR EBICS must be null
 * @param attributeType OZHNN = Signed files for DS, DZHNN = Not signed files
 */
class EbicsUploadOrder(
    val orderType: String? = null,
    val attributeType: AttributeType,
    params: Map<String, String>
) : AbstractEbicsUploadOrder(
    if (orderType == null) EbicsAdminOrderType.FUL else EbicsAdminOrderType.UPL,
    params
) {
    override fun toString(): String {
        return if (orderType == null)
            "AdminOrderType=$adminOrderType AttributeType=$attributeType"
        else
            "Ordertype=$orderType AttributeType=$attributeType"
    }
}



