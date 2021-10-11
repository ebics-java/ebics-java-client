package org.ebics.client.api.user

import org.ebics.client.api.getById
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.user.cert.UserKeyStoreService
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.user.permission.BankConnectionAccessType
import org.ebics.client.model.user.EbicsUserAction
import org.ebics.client.model.user.EbicsUserStatusEnum
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val partnerService: PartnerService,
    private val userKeyStoreService: UserKeyStoreService,
) {


    fun findUsers(permission: BankConnectionAccessType): List<User> {
        return userRepository.findAll().filter { it.hasAccess(permission, AuthenticationContext.fromSecurityContext()) }
    }

    fun getUserById(userId: Long, permission: BankConnectionAccessType = BankConnectionAccessType.READ): User {
        val bankConnection = userRepository.getById(userId, "bankconnection")
        bankConnection.checkAccess(permission)
        return bankConnection
    }

    fun saveUser(bankConnection: User): Long {
        bankConnection.checkWriteAccess()
        userRepository.saveAndFlush(bankConnection)
        return bankConnection.id!!
    }

    fun createUserAndPartner(bankConnection: BankConnection): Long {
        with(bankConnection) {
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            val authCtx = AuthenticationContext.fromSecurityContext()
            val user = User(
                null,
                ebicsVersion,
                userId,
                name,
                dn = DomainNameGenerator(name, Locale.getDefault()?.country).toString(),
                useCertificate = useCertificate,
                usePassword = false,
                partner = partner,
                keyStore = null,
                creator = authCtx.name,
                guestAccess = guestAccess
            )
            user.checkWriteAccess()
            userRepository.saveAndFlush(user)
            return user.id!!
        }
    }

    fun updateUserAndPartner(id: Long, bankConnection: BankConnection): Long {
        with(bankConnection) {
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            val currentUser = userRepository.getById(id, "bankconnection")
            currentUser.checkWriteAccess()
            //Depending on user status only some values are editable
            val updatedUser = when (currentUser.userStatus) {
                EbicsUserStatusEnum.CREATED, EbicsUserStatusEnum.NEW -> currentUser.updateFromBankConnectionBeforeInitialization(
                    bankConnection,
                    partner
                )
                else -> currentUser.updateFromBankConnectionAfterInitialization(bankConnection)
            }
            userRepository.saveAndFlush(updatedUser)
            return id
        }
    }

    private fun User.updateFromBankConnectionAfterInitialization(
        bankConnection: BankConnection
    ) = User(
        id,
        bankConnection.ebicsVersion,
        userId,
        bankConnection.name,
        dn,
        userStatus,
        useCertificate,
        usePassword,
        partner,
        keyStore,
        creator,
        bankConnection.guestAccess,
        traces
    )

    private fun User.updateFromBankConnectionBeforeInitialization(
        bankConnection: BankConnection,
        partner: Partner
    ) = User(
        id,
        bankConnection.ebicsVersion,
        bankConnection.userId,
        bankConnection.name,
        dn,
        userStatus,
        bankConnection.useCertificate,
        usePassword,
        partner,
        keyStore,
        creator,
        bankConnection.guestAccess,
        traces
    )

    fun deleteUser(userId: Long) {
        userRepository.getById(userId, "bankconnection").checkWriteAccess()
        userRepository.deleteById(userId)
    }


    /**
     * Resetting user status to default
     * After such reset must be user newly initialized, including creation of user keys
     */
    fun resetStatus(userId: Long): Unit {
        val user = userRepository.getById(userId, "bankconnection")
        user.checkWriteAccess()
        //Delete user key if available
        user.keyStore?.let { userKeyStoreService.deleteById(it.id!!) }
        //Set user status to CREATED
        user.updateStatus(EbicsUserAction.RESET)
        userRepository.saveAndFlush(user)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
    }
}