package org.ebics.client.order

import java.util.*

/**
 * Download order
 * @param adminOrderType the EBICS admin order type
 * @param startDate start date (for historical downloads only)
 * @param endDate end date (for historical downloads only)
 */
abstract class AbstractEbicsDownloadOrder
    (adminOrderType: EbicsAdminOrderType, val startDate: Date?, val endDate: Date?, params: Map<String, String>) :
    EbicsOrder(
        adminOrderType, params
    )