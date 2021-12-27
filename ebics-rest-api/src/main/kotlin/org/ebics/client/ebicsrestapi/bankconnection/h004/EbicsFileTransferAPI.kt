package org.ebics.client.ebicsrestapi.bankconnection.h004

import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.ebicsrestapi.bankconnection.UploadResponse
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionCache
import org.ebics.client.ebicsrestapi.utils.IFileDownloadCache
import org.ebics.client.filetransfer.h004.FileTransfer
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.Product
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.h004.EbicsDownloadOrder
import org.ebics.client.order.h004.EbicsUploadOrder
import org.ebics.client.order.h004.OrderType
import org.ebics.client.utils.toDate
import org.ebics.client.utils.toHexString
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component("EbicsFileTransferAPIH004")
class EbicsFileTransferAPI(
    private val sessionCache: IEbicsSessionCache,
    private val fileDownloadCache: IFileDownloadCache,
) {
    private val product =
        Product("EBICS 2.5 H004 REST API Client", "en", "org.jto.ebics")

    fun uploadFile(userId: Long, uploadRequest: UploadRequest, uploadFile: MultipartFile): UploadResponse {
        val session = sessionCache.getSession(UserIdPass(userId, uploadRequest.password), product)
        val order =
            EbicsUploadOrder(uploadRequest.orderType, uploadRequest.attributeType, uploadRequest.params ?: emptyMap())
        val response = FileTransfer(session).sendFile(uploadFile.bytes, order)
        return UploadResponse(response.orderNumber, response.transactionId.toHexString())
    }

    fun downloadFile(userId: Long, downloadRequest: DownloadRequest): ResponseEntity<Resource> {
        val session = sessionCache.getSession(UserIdPass(userId, downloadRequest.password), product)
        val order = EbicsDownloadOrder(
            downloadRequest.adminOrderType,
            downloadRequest.orderType,
            downloadRequest.startDate?.toDate(),
            downloadRequest.endDate?.toDate(),
            downloadRequest.params
        )
        val outputStream = FileTransfer(session).fetchFile(order)
        val resource = ByteArrayResource(outputStream.toByteArray())
        return ResponseEntity.ok().contentLength(resource.contentLength()).body(resource)
    }

    fun getOrderTypes(userId: Long, password: String): List<OrderType> {
        val session = sessionCache.getSession(UserIdPass(userId, password), product)

        val htdFileContent = fileDownloadCache.getLastFileCached(
            session,
            OrderTypeDefinition(EbicsAdminOrderType.HTD),
            EbicsVersion.H004
        )

        return FileTransfer(session).getOrderTypes(htdFileContent)
    }
}