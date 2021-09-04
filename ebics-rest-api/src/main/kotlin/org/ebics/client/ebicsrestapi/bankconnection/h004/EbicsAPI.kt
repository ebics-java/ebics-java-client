package org.ebics.client.ebicsrestapi.bankconnection.h004

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.bank.cert.BankKeyStoreService
import org.ebics.client.api.getById
import org.ebics.client.ebicsrestapi.EbicsRestConfiguration
import org.ebics.client.api.user.UserRepository
import org.ebics.client.ebicsrestapi.bankconnection.UploadResponse
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.filetransfer.h004.FileTransfer
import org.ebics.client.keymgmt.h004.KeyManagementImpl
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.Product
import org.ebics.client.order.h004.EbicsDownloadOrder
import org.ebics.client.order.h004.EbicsUploadOrder
import org.ebics.client.utils.toDate
import org.ebics.client.utils.toHexString
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component("EbicsAPIH004")
class EbicsAPI(
    private val userRepository: UserRepository,
    private val bankService: BankService,
    private val bankKeyStoreService: BankKeyStoreService,
    private val configuration: EbicsRestConfiguration)
{
    private val product =
        Product("EBICS 2.5 H004 REST API Client", "en", "org.jto.ebics")

    fun sendINI(userIdPass: UserIdPass) {
        val user = userRepository.getById(userIdPass.id, "bankconnection")
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val manager = toUserCertMgr( userIdPass.password )
            val session = EbicsSession(user, configuration, product, manager, null)
            KeyManagementImpl(session).sendINI(null)
            userRepository.saveAndFlush(user) //The state of user was changed after INI, must be persisted
        }
    }

    fun sendHIA(userIdPass: UserIdPass) {
        val user = userRepository.getById(userIdPass.id, "bankconnection")
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val manager = toUserCertMgr( userIdPass.password )
            val session = EbicsSession(user, configuration, product, manager, null)
            KeyManagementImpl(session).sendHIA(null)
            userRepository.saveAndFlush(user) //The state of user was changed after HIA, must be persisted
        }
    }

    fun sendHPB(userIdPass: UserIdPass) {
        val user = userRepository.getById(userIdPass.id, "bankconnection")
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val userCertManager = toUserCertMgr( userIdPass.password )
            val session = EbicsSession(user, configuration, product, userCertManager, null)
            val bankCertManager = KeyManagementImpl(session).sendHPB( userIdPass.password )
            val bankKeyStore = BankKeyStore.fromBankCertMgr(bankCertManager, user.partner.bank)
            bankKeyStoreService.save(bankKeyStore) //BankKeyStore must be saved
            bankService.updateKeyStore(user.partner.bank, bankKeyStore) //BankKeyStore must be added to bank
            userRepository.saveAndFlush(user) //The state of user was changed after HPB, must be persisted
        }
    }

    fun uploadFile(userId: Long, uploadRequest: UploadRequest, uploadFile: MultipartFile): UploadResponse {
        val user = userRepository.getById(userId, "bankconnection")
        with(requireNotNull(user.keyStore) { "User certificates must be first initialized" }) {
            val userCertManager = toUserCertMgr(uploadRequest.password)
            with (requireNotNull(user.partner.bank.keyStore) {"Bank certificates must be first initialized"}) {
                val bankCertManager = toBankCertMgr()
                val session = EbicsSession(user, configuration, product, userCertManager, bankCertManager)
                val order = EbicsUploadOrder(uploadRequest.orderType, uploadRequest.attributeType, uploadRequest.params ?: emptyMap())
                val response = FileTransfer(session).sendFile(uploadFile.bytes, order)
                return UploadResponse(response.orderNumber, response.transactionId.toHexString())
            }
        }
    }

    fun downloadFile(userId: Long, downloadRequest: DownloadRequest): ResponseEntity<Resource> {
        val user = userRepository.getById(userId, "bankconnection")
        with(requireNotNull(user.keyStore) { "User certificates must be first initialized" }) {
            val userCertManager = toUserCertMgr(downloadRequest.password)
            with (requireNotNull(user.partner.bank.keyStore) {"Bank certificates must be first initialized"}) {
                val bankCertManager = toBankCertMgr()
                val session = EbicsSession(user, configuration, product, userCertManager, bankCertManager)
                val order = EbicsDownloadOrder(downloadRequest.adminOrderType, downloadRequest.orderType, downloadRequest.startDate?.toDate(), downloadRequest.endDate?.toDate(), downloadRequest.params)
                val outputStream = ByteOutputStream()
                FileTransfer(session).fetchFile(order, outputStream)
                val resource = ByteArrayResource(outputStream.bytes)
                return ResponseEntity.ok().contentLength(resource.contentLength()).body(resource)
            }
        }
    }
}