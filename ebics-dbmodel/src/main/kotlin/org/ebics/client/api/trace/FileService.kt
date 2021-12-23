package org.ebics.client.api.trace

import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.user.User
import org.ebics.client.model.EbicsVersion

interface FileService {
    fun getLastDownloadedFile(
        orderType: OrderTypeDefinition,
        user: User,
        ebicsVersion: EbicsVersion,
        useSharedPartnerData: Boolean = true
    ): TraceEntry

    fun addTextFile(
        user: User,
        orderType: OrderTypeDefinition,
        fileContent: String,
        sessionId: String,
        orderNumber: String,
        ebicsVersion: EbicsVersion,
        upload: Boolean
    )
}