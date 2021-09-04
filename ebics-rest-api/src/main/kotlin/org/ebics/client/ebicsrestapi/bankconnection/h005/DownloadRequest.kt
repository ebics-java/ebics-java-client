package org.ebics.client.ebicsrestapi.bankconnection.h005

import org.ebics.client.ebicsrestapi.bankconnection.AbstractDownloadRequest
import org.ebics.client.order.EbicsService
import java.time.LocalDate

class DownloadRequest(
    password: String,
    val orderService: EbicsService,
    startDate: LocalDate?,
    endDate: LocalDate?,
    params: Map<String, String>?,
) : AbstractDownloadRequest(password, params, startDate, endDate)