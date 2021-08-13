package org.ebics.client.ebicsrestapi.bankconnection.h004

import org.ebics.client.ebicsrestapi.bankconnection.AbstractDownloadRequest
import java.time.LocalDate

class DownloadRequest(
    password: String,
    val orderType: String? = null,
    startDate: LocalDate?,
    endDate: LocalDate?,
    params: Map<String, String>,
) : AbstractDownloadRequest(password, params, startDate, endDate)