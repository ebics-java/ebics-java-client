package org.ebics.client.ebicsrestapi.h003

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.ebics.client.api.cert.UserKeyStoreRepository
import org.ebics.client.api.user.UserInfoRepository
import org.ebics.client.ebicsrestapi.EbicsRestConfiguration
import org.ebics.client.api.user.UserRepository
import org.ebics.client.certificate.CertificateManager
import org.ebics.client.keymgmt.h003.KeyManagementImpl
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.Product
import org.springframework.stereotype.Component

@Component
class EbicsAPI(
    private val userRepository: UserRepository,
    private val userInfoRepository: UserInfoRepository,
    private val configuration: EbicsRestConfiguration)
{
    private val product =
        Product("EBICS 2.4 H003 REST API Client", "en", "org.jto.ebics")

    fun sendINI(userId:Long, password:String) {
        val user = userRepository.getOne(userId)
        val userInfo = userInfoRepository.getOne(userId)
        with (requireNotNull ( userInfo.keyStore ) {"User certificates must be first initialized"}) {
            val manager = CertificateManager.load(ByteInputStream(keyStoreBytes, keyStoreBytes.size), password::toCharArray, userInfo.userId)
            val session = EbicsSession(user, configuration, product, manager, null)
            KeyManagementImpl(session).sendINI(null)
        }
    }
}