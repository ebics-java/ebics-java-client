package org.ebics.client.api.cert

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.ebics.client.api.user.UserInfo
import org.springframework.stereotype.Service
import java.io.OutputStream

@Service
class UserKeyStoreService(private val userKeyStoreRepository: UserKeyStoreRepository) {
    fun save(os: ByteOutputStream, userInfo: UserInfo) {
        userKeyStoreRepository.save(UserKeyStore(null, os.bytes, userInfo))
    }

    fun load(userId:Long):ByteInputStream {
        val bytes = userKeyStoreRepository.findAll().filter { it.userInfo.id ==  userId}.single().keyStoreBytes
        return ByteInputStream(bytes, bytes.size)
    }
}