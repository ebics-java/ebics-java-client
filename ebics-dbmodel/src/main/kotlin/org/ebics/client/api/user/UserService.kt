package org.ebics.client.api.user

import org.ebics.client.api.EbicsUserInfo
import org.ebics.client.api.FunctionException
import org.ebics.client.api.NotFoundException
import org.ebics.client.api.user.cert.UserKeyStore
import org.ebics.client.api.user.cert.UserKeyStoreService
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.model.user.EbicsUserAction
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val partnerService: PartnerService,
    private val userKeyStoreService: UserKeyStoreService,
) {
    fun findUsers(): List<User> = userRepository.findAll()

    fun getUserById(userId: Long): User {
        try {
            return userRepository.getOne(userId)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }

    fun createUserAndPartner(userInfo: EbicsUserInfo, ebicsPartnerId: String, bankId: Long): Long {
        val partner = partnerService.createOrGetPartner(ebicsPartnerId, bankId)
        val user = User(
            null, userInfo.ebicsVersion, userInfo.userId, userInfo.name, userInfo.dn,
            partner = partner, keyStore = null
        )
        userRepository.saveAndFlush(user)
        return user.id!!
    }

    fun updateUserAndPartner(userId: Long, userInfo: UserInfo, ebicsPartnerId: String, bankId: Long): Long {
        val partner = partnerService.createOrGetPartner(ebicsPartnerId, bankId)
        try {
            val currentUser = userRepository.getOne(userId)
            val updatedUser = User(
                userId,
                userInfo.ebicsVersion,
                userInfo.userId,
                userInfo.name,
                userInfo.dn,
                currentUser.userStatus,
                partner,
                currentUser.keyStore
            )
            userRepository.saveAndFlush(updatedUser)
            return userId
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }

    fun deleteUser(userId: Long) = userRepository.deleteById(userId)

    fun createOrUpdateUserCertificates(userId: Long, password: String):Long {
        try {
            val user = userRepository.getOne(userId)
            user.checkAction(EbicsUserAction.CREATE_KEYS)
            val userCertMgr = UserCertificateManager.create(user.dn)
            val userKeyStore = UserKeyStore.fromUserCertMgr(user, userCertMgr, password::toCharArray)
            userKeyStoreService.save(userKeyStore)
            user.keyStore = userKeyStore
            user.updateStatus(EbicsUserAction.CREATE_KEYS)
            userRepository.saveAndFlush(user)
            return userKeyStore.id!!
        } catch (ex: IllegalArgumentException) {
            throw FunctionException("Error creating certificate for user $userId", ex)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }

    fun resetStatus(userId: Long): Unit {
        try {
            val user = userRepository.getOne(userId)
            user.updateStatus(EbicsUserAction.RESET)
            userRepository.saveAndFlush(user)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }
}