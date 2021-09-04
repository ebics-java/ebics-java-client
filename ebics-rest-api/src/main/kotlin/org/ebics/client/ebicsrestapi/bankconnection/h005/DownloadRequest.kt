package org.ebics.client.ebicsrestapi.bankconnection.h005

import org.ebics.client.ebicsrestapi.bankconnection.AbstractDownloadRequest
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.EbicsService
import java.lang.IllegalArgumentException
import java.time.LocalDate

class DownloadRequest(
    password: String,
    adminOrderType: EbicsAdminOrderType = EbicsAdminOrderType.BTD,
    val orderService: EbicsService? = null,
    startDate: LocalDate?,
    endDate: LocalDate?,
    params: Map<String, String> = emptyMap(),
) : AbstractDownloadRequest(password, adminOrderType, params, startDate, endDate) {
    init {
        if (adminOrderType == EbicsAdminOrderType.BTD && orderService == null)
            throw IllegalArgumentException("The orderService is mandatory for standard file download (BTD)")
        if (adminOrderType != EbicsAdminOrderType.BTD && orderService != null)
            throw IllegalArgumentException("The orderService must be null for admin order types other than BTD")
    }
}