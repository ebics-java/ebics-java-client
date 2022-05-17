package org.ebics.client.ebicsrestapi.utils

import org.ebics.client.api.trace.IFileService
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.EbicsVersion
import org.springframework.stereotype.Service

@Service
class FileDownloadCache(private val fileService: IFileService) : IFileDownloadCache {
    override fun getLastFileCached(
        session: EbicsSession,
        orderType: OrderTypeDefinition,
        ebicsVersion: EbicsVersion,
        useCache: Boolean,
    ): ByteArray {
        return if (useCache) {
            try {
                //Try to get file from cache
                fileService.getLastDownloadedFile(
                    orderType,
                    session.user as BankConnectionEntity,
                    ebicsVersion
                ).messageBody.toByteArray()
            } catch (e: NoSuchElementException) {
                //In case the cache is empty we retrieve file online
                retrieveFileOnlineAndStoreToCache(session, orderType, ebicsVersion)
            }
        } else {
            //Cache is not required, so online request is made
            retrieveFileOnlineAndStoreToCache(session, orderType, ebicsVersion)
        }
    }

    override fun retrieveFileOnlineAndStoreToCache(
        session: EbicsSession,
        orderType: OrderTypeDefinition,
        ebicsVersion: EbicsVersion
    ): ByteArray {
        val outputStream =
            if (ebicsVersion == EbicsVersion.H005) {
                org.ebics.client.filetransfer.h005.FileTransferSession(session).fetchFile(
                    org.ebics.client.order.h005.EbicsDownloadOrder(
                        orderType.adminOrderType,
                        orderType.ebicsServiceType,
                        null,
                        null
                    )
                )
            } else {
                org.ebics.client.filetransfer.h004.FileTransferSession(session).fetchFile(
                    org.ebics.client.order.h004.EbicsDownloadOrder(
                        orderType.adminOrderType,
                        orderType.businessOrderType,
                        null,
                        null
                    )
                )
            }
        fileService.addDownloadedTextFile(
            session.user as BankConnectionEntity,
            orderType,
            String(outputStream.toByteArray()),
            session.sessionId,
            ebicsVersion
        )
        return outputStream.toByteArray()
    }
}