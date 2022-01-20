package org.ebics.client.api.trace

import org.ebics.client.api.EbicsSession
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.user.User
import org.ebics.client.model.EbicsVersion
import java.time.ZonedDateTime

interface IFileService {
    fun getLastDownloadedFile(
        orderType: OrderTypeDefinition,
        user: User,
        ebicsVersion: EbicsVersion,
        useSharedPartnerData: Boolean = true
    ): TraceEntry

    fun addDownloadedTextFile(
        user: User,
        orderType: OrderTypeDefinition,
        fileContent: String,
        sessionId: String,
        ebicsVersion: EbicsVersion,
    ) = addTextFile(user, orderType, fileContent, sessionId, null, ebicsVersion, false)

    fun addUploadedTextFile(
        session: EbicsSession,
        orderType: OrderTypeDefinition,
        fileContent: String,
        orderNumber: String,
        ebicsVersion: EbicsVersion,
    ) = addTextFile(session.user as User, orderType, fileContent, session.sessionId, orderNumber, ebicsVersion, true)

    fun addTextFile(
        user: User,
        orderType: OrderTypeDefinition,
        fileContent: String,
        sessionId: String,
        orderNumber: String?,
        ebicsVersion: EbicsVersion,
        upload: Boolean
    )

    fun removeAllFilesOlderThan(dateTime: ZonedDateTime)
}