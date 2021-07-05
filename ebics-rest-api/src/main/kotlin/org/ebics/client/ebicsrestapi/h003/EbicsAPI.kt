package org.ebics.client.ebicsrestapi.h003

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.ebics.client.api.bank.BankRepository
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.bank.cert.BankKeyStoreService
import org.ebics.client.ebicsrestapi.EbicsRestConfiguration
import org.ebics.client.api.user.UserRepository
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.ebicsrestapi.UserIdPass
import org.ebics.client.filetransfer.h003.FileTransfer
import org.ebics.client.keymgmt.h003.KeyManagementImpl
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.Product
import org.ebics.client.order.h003.EbicsUploadOrder
import org.springframework.stereotype.Component

@Component
class EbicsAPI(
    private val userRepository: UserRepository,
    private val bankService: BankService,
    private val bankKeyStoreService: BankKeyStoreService,
    private val configuration: EbicsRestConfiguration)
{
    private val product =
        Product("EBICS 2.4 H003 REST API Client", "en", "org.jto.ebics")

    fun sendINI(userIdPass: UserIdPass) {
        val user = userRepository.getOne(userIdPass.id)
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val manager = toUserCertMgr( userIdPass.passCb )
            val session = EbicsSession(user, configuration, product, manager, null)
            KeyManagementImpl(session).sendINI(null)
            userRepository.saveAndFlush(user) //The state of user was changed after INI, must be persisted
        }
    }

    fun sendHIA(userIdPass: UserIdPass) {
        val user = userRepository.getOne(userIdPass.id)
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val manager = toUserCertMgr( userIdPass.passCb )
            val session = EbicsSession(user, configuration, product, manager, null)
            KeyManagementImpl(session).sendHIA(null)
            userRepository.saveAndFlush(user) //The state of user was changed after HIA, must be persisted
        }
    }

    fun sendHPB(userIdPass: UserIdPass) {
        val user = userRepository.getOne(userIdPass.id)
        with (requireNotNull ( user.keyStore ) {"User certificates must be first initialized"}) {
            val userCertManager = toUserCertMgr( userIdPass.passCb )
            val session = EbicsSession(user, configuration, product, userCertManager, null)
            val bankCertManager = KeyManagementImpl(session).sendHPB( userIdPass.passCb )
            val bankKeyStore = BankKeyStore.fromBankCertMgr(bankCertManager, user.partner.bank)
            bankKeyStoreService.save(bankKeyStore) //BankKeyStore must be saved
            bankService.updateKeyStore(user.partner.bank, bankKeyStore) //BankKeyStore must be added to bank
            userRepository.saveAndFlush(user) //The state of user was changed after HPB, must be persisted
        }
    }

    fun uploadFile(userIdPass: UserIdPass) {
        val user = userRepository.getOne(userIdPass.id)
        with(requireNotNull(user.keyStore) { "User certificates must be first initialized" }) {
            val userCertManager = toUserCertMgr(userIdPass.passCb)
            with (requireNotNull(user.partner.bank.keyStore) {"Bank certificates must be first initialized"}) {
                val bankCertManager = toBankCertMgr()
                val session = EbicsSession(user, configuration, product, userCertManager, bankCertManager)
                val content = null
                //val order = EbicsUploadOrder()
                //FileTransfer(session).sendFile(content, order)
            }
        }
    }
}