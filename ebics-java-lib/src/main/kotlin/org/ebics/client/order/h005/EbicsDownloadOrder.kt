package org.ebics.client.order.h005

import org.ebics.client.order.AbstractEbicsDownloadOrder
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.EbicsService
import java.util.*

/**
 * H005 Download order
 * @param ebicsService the ECBIS service for BTU
 * @param startDate start date (for historical downloads only)
 * @param endDate end date (for historical downloads only)
 */
class EbicsDownloadOrder
(adminOrderType: EbicsAdminOrderType = EbicsAdminOrderType.BTD, val orderService: EbicsService?, startDate: Date?, endDate: Date?, params: Map<String, String>) :
    AbstractEbicsDownloadOrder(adminOrderType, startDate, endDate, params)