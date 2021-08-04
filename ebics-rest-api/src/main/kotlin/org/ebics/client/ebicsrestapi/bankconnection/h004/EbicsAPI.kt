package org.ebics.client.ebicsrestapi.bankconnection.h004

import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.bank.cert.BankKeyStoreService
import org.ebics.client.ebicsrestapi.EbicsRestConfiguration
import org.ebics.client.api.user.UserRepository
import org.ebics.client.ebicsrestapi.bankconnection.UploadInitResponse
import org.ebics.client.ebicsrestapi.bankconnection.UploadSegmentRequest
import org.ebics.client.ebicsrestapi.bankconnection.UploadSegmentResponse
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.keymgmt.h004.KeyManagementImpl
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.Product
import org.ebics.client.order.h004.EbicsUploadOrder
import org.springframework.stereotype.Component

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
        val user = userRepository.getOne(userIdPass.id)
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val manager = toUserCertMgr( userIdPass.password )
            val session = EbicsSession(user, configuration, product, manager, null)
            KeyManagementImpl(session).sendINI(null)
            userRepository.saveAndFlush(user) //The state of user was changed after INI, must be persisted
        }
    }

    fun sendHIA(userIdPass: UserIdPass) {
        val user = userRepository.getOne(userIdPass.id)
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val manager = toUserCertMgr( userIdPass.password )
            val session = EbicsSession(user, configuration, product, manager, null)
            KeyManagementImpl(session).sendHIA(null)
            userRepository.saveAndFlush(user) //The state of user was changed after HIA, must be persisted
        }
    }

    fun sendHPB(userIdPass: UserIdPass) {
        val user = userRepository.getOne(userIdPass.id)
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

    fun initFileUpload(userId: Long, uploadInitRequest: UploadInitRequest): UploadInitResponse {
        val user = userRepository.getOne(userId)
        with(requireNotNull(user.keyStore) { "User certificates must be first initialized" }) {
            val userCertManager = toUserCertMgr(uploadInitRequest.password)
            with (requireNotNull(user.partner.bank.keyStore) {"Bank certificates must be first initialized"}) {
                val bankCertManager = toBankCertMgr()
                val session = EbicsSession(user, configuration, product, userCertManager, bankCertManager)
                val content = null
                val order = EbicsUploadOrder(uploadInitRequest.orderType, uploadInitRequest.attributeType, uploadInitRequest.params)
                //FileTransfer(session).sendFile(content, order)
                return UploadInitResponse("transferId...", 512, "A0Z9")
            }
        }
    }

    fun uploadFileSegment(userId: Long, transferId: String, uploadSegmentRequest: UploadSegmentRequest): UploadSegmentResponse {
        return UploadSegmentResponse()
    }
}