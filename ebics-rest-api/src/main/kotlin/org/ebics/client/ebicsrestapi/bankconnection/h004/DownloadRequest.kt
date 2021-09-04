package org.ebics.client.ebicsrestapi.bankconnection.h004

import org.ebics.client.ebicsrestapi.bankconnection.AbstractDownloadRequest
import org.ebics.client.order.EbicsAdminOrderType
import java.lang.IllegalArgumentException
import java.time.LocalDate

class DownloadRequest(
    password: String,
    adminOrderType: EbicsAdminOrderType = EbicsAdminOrderType.DNL,
    val orderType: String? = null,
    startDate: LocalDate?,
    endDate: LocalDate?,
    params: Map<String, String> = emptyMap(),
) : AbstractDownloadRequest(password, adminOrderType, params, startDate, endDate) {
    init {
        if (adminOrderType == EbicsAdminOrderType.DNL) {
            if (orderType == null)
                throw IllegalArgumentException("The orderType is mandatory for standard file download (DNL)")
            if (params.isNotEmpty())
                throw IllegalArgumentException("The params must be null for standard file download (DNL)")
        }
        if (adminOrderType == EbicsAdminOrderType.FDL) {
            if (params.isEmpty())
                throw IllegalArgumentException("The params are mandatory for standard file download (FDL)")
            if (orderType != null)
                throw IllegalArgumentException("The orderType must be null for standard file download (DNL)")
        }
    }
}