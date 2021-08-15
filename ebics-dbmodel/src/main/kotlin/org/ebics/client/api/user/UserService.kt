package org.ebics.client.api.user

import org.ebics.client.api.EbicsUser
import org.ebics.client.api.FunctionException
import org.ebics.client.api.NotFoundException
import org.ebics.client.api.user.cert.UserKeyStore
import org.ebics.client.api.user.cert.UserKeyStoreService
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.letter.DefaultLetterManager
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserAction
import org.ebics.client.model.user.EbicsUserStatusEnum
import org.slf4j.LoggerFactory
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val partnerService: PartnerService,
    private val userKeyStoreService: UserKeyStoreService,
) {


    fun findUsers(): List<User> {
        return userRepository.findAll().filter { Authorization.isAuthorizedForUserRead(it) }
    }

    fun getUserById(userId: Long): User {
        try {
            return userRepository.getOne(userId)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }

    private fun checkUserPartnerBankValidity(bankConnection: BankConnection) {
        with(bankConnection) {
            if (ebicsVersion == EbicsVersion.H005 && !useCertificate)
                throw FunctionException(
                    "User $userId validation failed, useCertificate must be set to true for EBICS version H005",
                    null
                )
        }
    }

    fun createUserAndPartner(bankConnection: BankConnection): Long {
        with(bankConnection) {
            checkUserPartnerBankValidity(this)
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            val authentication = SecurityContextHolder.getContext().authentication
            val user = User(
                null, ebicsVersion, userId, name, dn, useCertificate = useCertificate, usePassword = usePassword,
                partner = partner, keyStore = null, creator = authentication.name, guestAccess = guestAccess
            )
            Authorization.checkWriteAuthorization(user, authentication)
            userRepository.saveAndFlush(user)
            return user.id!!
        }
    }

    fun updateUserAndPartner(id: Long, bankConnection: BankConnection): Long {
        with(bankConnection) {
            checkUserPartnerBankValidity(this)
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            try {
                val currentUser = userRepository.getOne(id)
                Authorization.checkWriteAuthorization(currentUser)
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
                        currentUser.keyStore,
                        currentUser.name,
                        guestAccess
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
                        currentUser.keyStore,
                        currentUser.name,
                        guestAccess
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
                        currentUser.keyStore,
                        currentUser.name,
                        guestAccess
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
            Authorization.checkWriteAuthorization(userRepository.getOne(userId))
            userRepository.deleteById(userId)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }



    /**
     * Resetting user status to default
     * After such reset must be user newly initialized, including creation of user keys
     */
    fun resetStatus(userId: Long): Unit {
        try {
            val user = userRepository.getOne(userId)
            Authorization.checkWriteAuthorization(user)
            //Delete user key if available
            user.keyStore?.let { userKeyStoreService.deleteById(it.id!!) }
            //Set user status to CREATED
            user.updateStatus(EbicsUserAction.RESET)
            userRepository.saveAndFlush(user)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(userId, "user", ex)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
    }
}