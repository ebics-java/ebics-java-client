package org.ebics.client.ebicsrestapi.bankconnection.h004

import org.ebics.client.api.trace.IFileService
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.ebicsrestapi.bankconnection.UploadResponse
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionFactory
import org.ebics.client.ebicsrestapi.utils.IFileDownloadCache
import org.ebics.client.filetransfer.h004.FileTransferSession
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.h004.EbicsDownloadOrder
import org.ebics.client.order.h004.EbicsUploadOrder
import org.ebics.client.order.h004.OrderType
import org.ebics.client.utils.toDate
import org.ebics.client.xml.h004.HTDResponseOrderDataElement
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component("EbicsFileTransferAPIH004")
class EbicsFileTransferAPI(
    private val sessionFactory: IEbicsSessionFactory,
    private val fileDownloadCache: IFileDownloadCache,
    private val fileService: IFileService,
) {

    fun uploadFile(userId: Long, uploadRequest: UploadRequest, uploadFile: MultipartFile): UploadResponse {
        val session = sessionFactory.getSession(UserIdPass(userId, uploadRequest.password))
        val order =
            EbicsUploadOrder(uploadRequest.orderType, uploadRequest.attributeType, uploadRequest.params ?: emptyMap())
        val response = FileTransferSession(session).sendFile(uploadFile.bytes, order)
        fileService.addUploadedTextFile(
            session,
            OrderTypeDefinition(EbicsAdminOrderType.BTU, businessOrderType = uploadRequest.orderType),
            String(uploadFile.bytes),
            response.orderNumber,
            EbicsVersion.H004
        )
        return UploadResponse(response.orderNumber, response.transactionId)
    }

    fun downloadFile(userId: Long, downloadRequest: DownloadRequest): ResponseEntity<Resource> {
        val session = sessionFactory.getSession(UserIdPass(userId, downloadRequest.password))
        val order = EbicsDownloadOrder(
            downloadRequest.adminOrderType,
            downloadRequest.orderType,
            downloadRequest.startDate?.toDate(),
            downloadRequest.endDate?.toDate(),
            downloadRequest.params
        )
        val outputStream = FileTransferSession(session).fetchFile(order)
        val resource = ByteArrayResource(outputStream.toByteArray())
        return ResponseEntity.ok().contentLength(resource.contentLength()).body(resource)
    }

    fun getOrderTypes(userId: Long, password: String, useCache: Boolean): List<OrderType> {
        val session = sessionFactory.getSession(UserIdPass(userId, password))

        val htdFileContent = fileDownloadCache.getLastFileCached(
            session,
            OrderTypeDefinition(EbicsAdminOrderType.HTD),
            EbicsVersion.H004,
            useCache
        )

        return HTDResponseOrderDataElement.getOrderTypes(htdFileContent)
    }
}