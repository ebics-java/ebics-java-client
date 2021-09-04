package org.ebics.client.ebicsrestapi.bankconnection

import java.time.LocalDate

abstract class AbstractDownloadRequest(
    val password: String,
    val params: Map<String, String>? = emptyMap(),
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)