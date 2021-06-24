package org.ebics.client.api.cert

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.ebics.client.api.user.User
import org.springframework.stereotype.Service

@Service
class UserKeyStoreService(private val userKeyStoreRepository: UserKeyStoreRepository) {
    fun save(os: ByteOutputStream, user: User) {
        userKeyStoreRepository.save(UserKeyStore(null, os.bytes, user))
    }

    fun load(userId:Long):ByteInputStream {
        val bytes = userKeyStoreRepository.getKeyStoreByUserId(userId).keyStoreBytes
        return ByteInputStream(bytes, bytes.size)
    }
}