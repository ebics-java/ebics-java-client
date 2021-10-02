package org.ebics.client.api.user

import org.ebics.client.api.BankConnectionPermission
import org.ebics.client.api.getById
import org.ebics.client.api.user.cert.UserKeyStoreService
import org.ebics.client.api.partner.PartnerService
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


    fun findUsers(permission: BankConnectionPermission): List<User> {
        return userRepository.findAll().filter { SecurityCtxHelper.isAuthorizedFor(it, permission) }
    }

    fun getUserById(userId: Long, permission: BankConnectionPermission = BankConnectionPermission.READ): User {
        val bankConnection = userRepository.getById(userId, "bankconnection")
        SecurityCtxHelper.checkAuthorization(bankConnection, permission)
        return bankConnection
    }

    fun saveUser(bankConnection: User): Long {
        SecurityCtxHelper.checkWriteAuthorization(bankConnection)
        userRepository.saveAndFlush(bankConnection)
        return bankConnection.id!!
    }

    fun createUserAndPartner(bankConnection: BankConnection): Long {
        with(bankConnection) {
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            val authentication = SecurityCtxHelper.getAuthentication()
            val cn = name.toLowerCase().replace("[^\\x00-\\x7F]".toRegex(), "").replace("\\s".toRegex(), "-")
            val country =
                if (Locale.getDefault()?.country?.isNotBlank() == true) ",c=${Locale.getDefault().country.toLowerCase()}" else ""
            val user = User(
                null,
                ebicsVersion,
                userId,
                name,
                dn = "cn=$cn$country",
                useCertificate = useCertificate,
                usePassword = false,
                partner = partner,
                keyStore = null,
                creator = authentication.name,
                guestAccess = guestAccess
            )
            SecurityCtxHelper.checkWriteAuthorization(user, authentication)
            userRepository.saveAndFlush(user)
            return user.id!!
        }
    }

    fun updateUserAndPartner(id: Long, bankConnection: BankConnection): Long {
        with(bankConnection) {
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            val currentUser = userRepository.getById(id, "bankconnection")
            SecurityCtxHelper.checkWriteAuthorization(currentUser)
            //Depending on user status only some values are editable
            val updatedUser = when (currentUser.userStatus) {
                EbicsUserStatusEnum.CREATED -> User(
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
                    currentUser.creator,
                    guestAccess,
                    currentUser.traces
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
                    currentUser.creator,
                    guestAccess,
                    currentUser.traces
                )
                else -> User(
                    id,
                    ebicsVersion,
                    currentUser.userId,
                    name,
                    currentUser.dn,
                    currentUser.userStatus,
                    useCertificate,
                    currentUser.usePassword,
                    currentUser.partner,
                    currentUser.keyStore,
                    currentUser.creator,
                    guestAccess,
                    currentUser.traces
                )
            }
            userRepository.saveAndFlush(updatedUser)
            return id
        }
    }

    fun deleteUser(userId: Long) {
        SecurityCtxHelper.checkWriteAuthorization(userRepository.getById(userId, "bankconnection"))
        userRepository.deleteById(userId)
    }


    /**
     * Resetting user status to default
     * After such reset must be user newly initialized, including creation of user keys
     */
    fun resetStatus(userId: Long): Unit {
        val user = userRepository.getById(userId, "bankconnection")
        SecurityCtxHelper.checkWriteAuthorization(user)
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