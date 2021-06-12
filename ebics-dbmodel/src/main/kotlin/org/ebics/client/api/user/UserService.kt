package org.ebics.client.api.user

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.ebics.client.api.cert.UserKeyStoreService
import org.ebics.client.api.partner.PartnerRepository
import org.ebics.client.certificate.UserCertificateManager
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userInfoRepository: UserInfoRepository,
    private val partnerRepository: PartnerRepository,
    private val userKeyStoreService: UserKeyStoreService,
) {

    fun findUsers(): List<User> = userRepository.findAll()

    fun createUserInfo(user: UserInfo): Long {
        userInfoRepository.saveAndFlush(user)
        return user.id!!
    }

    fun delete(userId: Long) = userRepository.deleteById(userId)

    fun createUserCertificates(userId: Long, password:String) {
        val userInfo = userInfoRepository.getOne(userId)
        val os = ByteOutputStream()
        UserCertificateManager.create(userInfo.dn).save(os, password::toCharArray, userInfo.userId)
        userKeyStoreService.save(os, userInfo)
    }
}