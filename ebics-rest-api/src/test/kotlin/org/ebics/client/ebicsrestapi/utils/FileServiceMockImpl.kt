package org.ebics.client.ebicsrestapi.utils

import org.ebics.client.api.trace.IFileService
import org.ebics.client.api.trace.TraceEntry
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class FileServiceMockImpl : IFileService {
    var fileContent: String? = null

    override fun getLastDownloadedFile(
        orderType: OrderTypeDefinition,
        user: BankConnectionEntity,
        ebicsVersion: EbicsVersion,
        useSharedPartnerData: Boolean
    ): TraceEntry {
        if (fileContent == null)
            throw NoSuchElementException("Mock: No value cached yet")
        else
            return TraceEntry(1, "$fileContent-cached", user, "1",
            "CCCX", ebicsVersion, false, creator = "jan", orderType = orderType)
    }

    override fun addTextFile(
        user: BankConnectionEntity,
        orderType: OrderTypeDefinition,
        fileContent: String,
        sessionId: String,
        orderNumber: String?,
        ebicsVersion: EbicsVersion,
        upload: Boolean
    ) {
        this.fileContent = fileContent
    }

    override fun removeAllFilesOlderThan(dateTime: ZonedDateTime) {
        fileContent = null
    }
}