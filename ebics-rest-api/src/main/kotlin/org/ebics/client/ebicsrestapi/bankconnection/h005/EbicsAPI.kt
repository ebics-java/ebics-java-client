package org.ebics.client.ebicsrestapi.bankconnection.h005

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.ebics.client.api.user.permission.BankConnectionAccessType
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.bank.cert.BankKeyStoreService
import org.ebics.client.api.user.User
import org.ebics.client.ebicsrestapi.EbicsRestConfiguration
import org.ebics.client.api.user.UserServiceImpl
import org.ebics.client.ebicsrestapi.bankconnection.UploadResponse
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.session.EbicsSessionCache
import org.ebics.client.filetransfer.h005.FileTransfer
import org.ebics.client.keymgmt.h005.KeyManagementImpl
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.Product
import org.ebics.client.order.h005.EbicsDownloadOrder
import org.ebics.client.order.h005.EbicsUploadOrder
import org.ebics.client.order.h005.OrderType
import org.ebics.client.utils.toDate
import org.ebics.client.utils.toHexString
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component("EbicsAPIH005")
class EbicsAPI(
    private val userService: UserServiceImpl,
    private val bankService: BankService,
    private val bankKeyStoreService: BankKeyStoreService,
    private val sessionCache: EbicsSessionCache
) {
    private val product =
        Product("EBICS 3.0 H005 REST API Client", "en", "org.jto.ebics")

    fun sendINI(userIdPass: UserIdPass) {
        val session = sessionCache.getSession(userIdPass, product, false)
        KeyManagementImpl(session).sendINI(null)
        //The state of user was changed after INI, must be persisted
        userService.saveUser(session.user as User)
    }

    fun sendHIA(userIdPass: UserIdPass) {
        val session = sessionCache.getSession(userIdPass, product, false)
        KeyManagementImpl(session).sendHIA(null)
        //The state of user was changed after HIA, must be persisted
        userService.saveUser(session.user as User)
    }

    fun sendHPB(userIdPass: UserIdPass) {
        val session = sessionCache.getSession(userIdPass, product, false)
        val bankCertManager = KeyManagementImpl(session).sendHPB(userIdPass.password)
        val user = session.user as User
        val bankKeyStore = BankKeyStore.fromBankCertMgr(bankCertManager, user.partner.bank)
        bankKeyStoreService.save(bankKeyStore) //BankKeyStore must be saved
        bankService.updateKeyStore(user.partner.bank, bankKeyStore) //BankKeyStore must be added to bank
        userService.saveUser(user) //The state of user was changed after HPB, must be persisted
    }

    fun uploadFile(userId: Long, uploadRequest: UploadRequest, uploadFile: MultipartFile): UploadResponse {
        val session = sessionCache.getSession(UserIdPass(userId, uploadRequest.password), product)
        val order = EbicsUploadOrder(
            uploadRequest.orderService,
            uploadRequest.signatureFlag,
            uploadRequest.edsFlag,
            uploadRequest.fileName,
            uploadRequest.params ?: emptyMap()
        )
        val response = FileTransfer(session).sendFile(uploadFile.bytes, order)
        return UploadResponse(response.orderNumber, response.transactionId.toHexString())
    }

    fun downloadFile(userId: Long, downloadRequest: DownloadRequest): ResponseEntity<Resource> {
        val session = sessionCache.getSession(UserIdPass(userId, downloadRequest.password), product)

        val order = EbicsDownloadOrder(
            downloadRequest.adminOrderType,
            downloadRequest.orderService,
            downloadRequest.startDate?.toDate(),
            downloadRequest.endDate?.toDate(),
            downloadRequest.params
        )
        val outputStream = ByteOutputStream()
        FileTransfer(session).fetchFile(order, outputStream)
        val resource = ByteArrayResource(outputStream.bytes)
        return ResponseEntity.ok().contentLength(resource.contentLength()).body(resource)
    }

    fun getOrderTypes(userId: Long, password: String): List<OrderType> {
        val session = sessionCache.getSession(UserIdPass(userId, password), product)
        return FileTransfer(session).getOrderTypes()
    }
}