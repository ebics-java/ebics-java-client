package org.ebics.client.api.user.cert

import org.ebics.client.api.EbicsUser
import org.ebics.client.api.FunctionException
import org.ebics.client.api.NotFoundException
import org.ebics.client.api.user.SecurityCtxHelper
import org.ebics.client.api.user.UserRepository
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.letter.DefaultLetterManager
import org.ebics.client.model.user.EbicsUserAction
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.*

@Service
class UserCertificateService(val userRepository: UserRepository, val userKeyStoreService: UserKeyStoreService) {
    fun createOrUpdateUserCertificates(userId: Long, password: String): Long {
        try {
            val user = userRepository.getById(userId, "bankconnection")
            SecurityCtxHelper.checkWriteAuthorization(user)
            user.checkAction(EbicsUserAction.CREATE_KEYS)
            val userCertMgr = UserCertificateManager.create(user.dn)
            val userKeyStore = UserKeyStore.fromUserCertMgr(user, userCertMgr, password)
            userKeyStoreService.save(userKeyStore)
            user.keyStore = userKeyStore
            user.updateStatus(EbicsUserAction.CREATE_KEYS)
            userRepository.saveAndFlush(user)
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
        val user = userRepository.getById(userId, "bankconnection")
        SecurityCtxHelper.checkWriteAuthorization(user)
        user.checkAction(EbicsUserAction.CREATE_LETTERS)
        with(requireNotNull(user.keyStore) { "User certificates must be first initialized" }) {
            val userCertManager = toUserCertMgr(password)
            return createLetters(user, userCertManager)
        }
    }
}