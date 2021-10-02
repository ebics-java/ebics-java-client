package org.ebics.client.ebicsrestapi.bankconnection.h005

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.apache.xmlbeans.XmlObject
import org.ebics.client.api.BankConnectionPermission
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.bank.cert.BankKeyStoreService
import org.ebics.client.api.getById
import org.ebics.client.api.user.SecurityCtxHelper
import org.ebics.client.ebicsrestapi.EbicsRestConfiguration
import org.ebics.client.api.user.UserRepository
import org.ebics.client.api.user.UserService
import org.ebics.client.ebicsrestapi.bankconnection.UploadResponse
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.filetransfer.h005.FileTransfer
import org.ebics.client.keymgmt.h005.KeyManagementImpl
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.Product
import org.ebics.client.order.AuthorisationLevel
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.EbicsMessage
import org.ebics.client.order.EbicsService
import org.ebics.client.order.h005.ContainerType
import org.ebics.client.order.h005.EbicsDownloadOrder
import org.ebics.client.order.h005.EbicsUploadOrder
import org.ebics.client.order.h005.OrderType
import org.ebics.client.utils.toDate
import org.ebics.client.utils.toHexString
import org.ebics.schema.h005.AuthOrderInfoType
import org.ebics.schema.h005.HTDReponseOrderDataType
import org.ebics.schema.h005.UserPermissionType
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component("EbicsAPIH005")
class EbicsAPI(
    private val userService: UserService,
    private val bankService: BankService,
    private val bankKeyStoreService: BankKeyStoreService,
    private val configuration: EbicsRestConfiguration)
{
    private val product =
        Product("EBICS 3.0 H005 REST API Client", "en", "org.jto.ebics")

    fun sendINI(userIdPass: UserIdPass) {
        val user = userService.getUserById(userIdPass.id, BankConnectionPermission.WRITE)
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val manager = toUserCertMgr( userIdPass.password )
            val session = EbicsSession(user, configuration, product, manager, null)
            KeyManagementImpl(session).sendINI(null)
            userService.saveUser(user) //The state of user was changed after INI, must be persisted
        }
    }

    fun sendHIA(userIdPass: UserIdPass) {
        val user = userService.getUserById(userIdPass.id, BankConnectionPermission.WRITE)
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val manager = toUserCertMgr( userIdPass.password )
            val session = EbicsSession(user, configuration, product, manager, null)
            KeyManagementImpl(session).sendHIA(null)
            userService.saveUser(user) //The state of user was changed after HIA, must be persisted
        }
    }

    fun sendHPB(userIdPass: UserIdPass) {
        val user = userService.getUserById(userIdPass.id, BankConnectionPermission.WRITE)
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val userCertManager = toUserCertMgr( userIdPass.password )
            val session = EbicsSession(user, configuration, product, userCertManager, null)
            val bankCertManager = KeyManagementImpl(session).sendHPB( userIdPass.password )
            val bankKeyStore = BankKeyStore.fromBankCertMgr(bankCertManager, user.partner.bank)
            bankKeyStoreService.save(bankKeyStore) //BankKeyStore must be saved
            bankService.updateKeyStore(user.partner.bank, bankKeyStore) //BankKeyStore must be added to bank
            userService.saveUser(user) //The state of user was changed after HPB, must be persisted
        }
    }

    fun uploadFile(userId: Long, uploadRequest: UploadRequest, uploadFile: MultipartFile): UploadResponse {
        val user = userService.getUserById(userId, BankConnectionPermission.USE)
        with(requireNotNull(user.keyStore) { "User certificates must be first initialized" }) {
            val userCertManager = toUserCertMgr(uploadRequest.password)
            with (requireNotNull(user.partner.bank.keyStore) {"Bank certificates must be first initialized"}) {
                val bankCertManager = toBankCertMgr()
                val session = EbicsSession(user, configuration, product, userCertManager, bankCertManager)
                val order = EbicsUploadOrder(uploadRequest.orderService, uploadRequest.signatureFlag, uploadRequest.edsFlag, uploadRequest.fileName, uploadRequest.params ?: emptyMap())
                val response = FileTransfer(session).sendFile(uploadFile.bytes, order)
                return UploadResponse(response.orderNumber, response.transactionId.toHexString())
            }
        }
    }

    fun downloadFile(userId: Long, downloadRequest: DownloadRequest): ResponseEntity<Resource> {
        val user = userService.getUserById(userId, BankConnectionPermission.USE)
        with(requireNotNull(user.keyStore) { "User certificates must be first initialized" }) {
            val userCertManager = toUserCertMgr(downloadRequest.password)
            with (requireNotNull(user.partner.bank.keyStore) {"Bank certificates must be first initialized"}) {
                val bankCertManager = toBankCertMgr()
                val session = EbicsSession(user, configuration, product, userCertManager, bankCertManager)
                val order = EbicsDownloadOrder(downloadRequest.adminOrderType, downloadRequest.orderService, downloadRequest.startDate?.toDate(), downloadRequest.endDate?.toDate(), downloadRequest.params)
                val outputStream = ByteOutputStream()
                FileTransfer(session).fetchFile(order, outputStream)
                val resource = ByteArrayResource(outputStream.bytes)
                return ResponseEntity.ok().contentLength(resource.contentLength()).body(resource)
            }
        }
    }

    fun getOrderTypes(userId: Long, password: String): List<OrderType> {
        val user = userService.getUserById(userId, BankConnectionPermission.USE)
        with(requireNotNull(user.keyStore) { "User certificates must be first initialized" }) {
            val userCertManager = toUserCertMgr(password)
            with (requireNotNull(user.partner.bank.keyStore) {"Bank certificates must be first initialized"}) {
                val bankCertManager = toBankCertMgr()
                val session = EbicsSession(user, configuration, product, userCertManager, bankCertManager)
                return FileTransfer(session).getOrderTypes()
            }
        }
    }
}