package org.ebics.client.api.bankconnection.cert

import org.ebics.client.api.EbicsUser
import org.ebics.client.api.FunctionException
import org.ebics.client.api.getById
import org.ebics.client.api.bankconnection.BankConnectionRepository
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.letter.DefaultLetterManager
import org.ebics.client.model.user.EbicsUserAction
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.*

@Service
class UserCertificateService(val bankConnectionRepository: BankConnectionRepository, val userKeyStoreService: UserKeyStoreService) {
    fun createOrUpdateUserCertificates(userId: Long, certRequest: CertRequest): Long {
        try {
            val user = bankConnectionRepository.getById(userId, "bankconnection")
            user.checkWriteAccess()
            user.checkAction(EbicsUserAction.CREATE_KEYS)
            val userCertMgr = UserCertificateManager.create(certRequest.dn)
            val userKeyStore = UserKeyStore.fromUserCertMgr(user, userCertMgr, certRequest.password)
            userKeyStoreService.save(userKeyStore)
            user.dn = certRequest.dn
            user.keyStore = userKeyStore
            user.usePassword = certRequest.usePassword
            user.updateStatus(EbicsUserAction.CREATE_KEYS)
            bankConnectionRepository.saveAndFlush(user)
            return userKeyStore.id!!
        } catch (ex: IllegalArgumentException) {
            throw FunctionException("Error creating certificate for user $userId", ex)
        }
    }

    private fun createLetters(user: EbicsUser, userCert: UserCertificateManager): CertificateLetters {
        with(DefaultLetterManager(Locale.getDefault())) {
            val signature = createA005Letter(user, userCert)
            val encryption = createE002Letter(user, userCert)
            val authentication = createX002Letter(user, userCert)
            return CertificateLetters(
                Letter(signature.toStr(), String(signature.hash)),
                Letter(encryption.toStr(), String(encryption.hash)),
                Letter(authentication.toStr(), String(authentication.hash))
            )
        }
    }

    fun getUserLetters(userId: Long, password: String): CertificateLetters {
        val user = bankConnectionRepository.getById(userId, "bankconnection")
        user.checkWriteAccess()
        user.checkAction(EbicsUserAction.CREATE_LETTERS)
        with(requireNotNull(user.keyStore) { "User certificates must be first initialized" }) {
            val userCertManager = toUserCertMgr(password)
            return createLetters(user, userCertManager)
        }
    }
}