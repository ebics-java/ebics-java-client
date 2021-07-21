package org.ebics.client.api.user

import org.ebics.client.api.FunctionException
import org.ebics.client.api.NotFoundException
import org.ebics.client.api.user.cert.UserKeyStore
import org.ebics.client.api.user.cert.UserKeyStoreService
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserAction
import org.ebics.client.model.user.EbicsUserStatusEnum
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

    fun checkUserPartnerBankValidity(userPartnerBank: UserPartnerBank) {
        with(userPartnerBank) {
            if (ebicsVersion == EbicsVersion.H005 && !useCertificate)
                throw FunctionException("User $userId validation failed, useCertificate must be set to true for EBICS version H005", null)
        }
    }

    fun createUserAndPartner(userPartnerBank: UserPartnerBank): Long {
        with(userPartnerBank) {
            checkUserPartnerBankValidity(this)
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            val user = User(
                null, ebicsVersion, userId, name, dn, useCertificate = useCertificate, usePassword = usePassword,
                partner = partner, keyStore = null
            )
            userRepository.saveAndFlush(user)
            return user.id!!
        }
    }

    fun updateUserAndPartner(id: Long, userPartnerBank: UserPartnerBank): Long {
        with(userPartnerBank) {
            checkUserPartnerBankValidity(this)
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            try {
                val currentUser = userRepository.getOne(id)
                //Depending on user status only some values are editable
                val updatedUser = when (currentUser.userStatus) {
                    EbicsUserStatusEnum.CREATED -> User(
                        id,
                        ebicsVersion,
                        userId,
                        name,
                        dn,
                        currentUser.userStatus,
                        useCertificate,
                        usePassword,
                        partner,
                        currentUser.keyStore
                    )
                    EbicsUserStatusEnum.NEW -> User(
                        id,
                        ebicsVersion,
                        userId,
                        name,
                        currentUser.dn,
                        currentUser.userStatus,
                        useCertificate,
                        currentUser.usePassword,
                        partner,
                        currentUser.keyStore
                    )
                    else -> User(
                        id,
                        currentUser.ebicsVersion,
                        currentUser.userId,
                        name,
                        currentUser.dn,
                        currentUser.userStatus,
                        currentUser.useCertificate,
                        currentUser.usePassword,
                        currentUser.partner,
                        currentUser.keyStore
                    )
                }
                userRepository.saveAndFlush(updatedUser)
                return id
            } catch (ex: ObjectRetrievalFailureException) {
                throw NotFoundException(id, "user", ex)
            }
        }
    }

    fun deleteUser(userId: Long) {
        try {
            userRepository.deleteById(userId)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }

    fun createOrUpdateUserCertificates(userId: Long, password: String): Long {
        try {
            val user = userRepository.getOne(userId)
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
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }

    fun resetStatus(userId: Long): Unit {
        try {
            val user = userRepository.getOne(userId)
            //Delete user key if available
            user.keyStore?.let { userKeyStoreService.deleteById(it.id!!) }
            //Set user status to CREATED
            user.updateStatus(EbicsUserAction.RESET)
            userRepository.saveAndFlush(user)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }
}