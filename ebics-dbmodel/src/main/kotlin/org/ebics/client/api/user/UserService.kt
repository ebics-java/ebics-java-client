package org.ebics.client.api.user

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.ebics.client.api.EbicsUserInfo
import org.ebics.client.api.cert.UserKeyStoreService
import org.ebics.client.api.partner.PartnerRepository
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.certificate.UserCertificateManager
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val partnerService: PartnerService,
    private val userKeyStoreService: UserKeyStoreService,
) {

    fun findUsers(): List<User> = userRepository.findAll()

    fun getUserById(userId: Long) = userRepository.getOne(userId)

    fun createUser(user: User): Long {
        userRepository.saveAndFlush(user)
        return user.id!!
    }

    fun createUser(userInfo: EbicsUserInfo, ebicsPartnerId: String, bankId: Long): Long {
        val partner = partnerService.createOrGetPartner(ebicsPartnerId, bankId)
        val user = User(null, userInfo.ebicsVersion, userInfo.userId, userInfo.name, userInfo.dn, userInfo.userStatus,
            partner, null)
        userRepository.saveAndFlush(user)
        return user.id!!
    }

    fun deleteUser(userId: Long) = userRepository.deleteById(userId)

    fun createUserCertificates(userId: Long, password:String) {
        val user = userRepository.getOne(userId)
        val os = ByteOutputStream()
        UserCertificateManager.create(user.dn).save(os, password::toCharArray, user.userId)
        userKeyStoreService.save(os, user)
    }
}