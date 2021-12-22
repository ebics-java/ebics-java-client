package org.ebics.client.api.trace

import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.user.User
import org.springframework.lang.Nullable

interface FileService {
    fun getLastDownloadedFile(orderType: OrderTypeDefinition, user: User, useSharedPartnerData: Boolean = true): TraceEntry
    fun addTextFile(user: User, orderType: OrderTypeDefinition, fileContent: String)
}