package org.ebics.client.order.h004

import org.ebics.client.order.AbstractEbicsDownloadOrder
import org.ebics.client.order.EbicsAdminOrderType
import java.util.*

/**
 * H003, H004 DE/FR Download orders
 * @param orderType the EBICS order type for DE EBICS, for FR EBICS is null
 * @param startDate start date (for historical downloads only)
 * @param endDate end date (for historical downloads only)
 */
class EbicsDownloadOrder(val orderType: String?, startDate: Date?, endDate: Date?, params: Map<String, String>) : AbstractEbicsDownloadOrder(
        if (orderType == null) EbicsAdminOrderType.FDL else EbicsAdminOrderType.DNL,
        startDate,
        endDate,
        params
    )

