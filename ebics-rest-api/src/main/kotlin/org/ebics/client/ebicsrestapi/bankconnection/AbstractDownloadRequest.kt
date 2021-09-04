package org.ebics.client.ebicsrestapi.bankconnection

import org.ebics.client.order.EbicsAdminOrderType
import java.time.LocalDate

abstract class AbstractDownloadRequest(
    val password: String,
    val adminOrderType: EbicsAdminOrderType,
    val params: Map<String, String>,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)