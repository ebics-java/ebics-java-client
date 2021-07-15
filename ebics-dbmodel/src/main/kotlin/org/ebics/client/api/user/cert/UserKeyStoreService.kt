package org.ebics.client.api.user.cert

import org.apache.xml.security.Init
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.api.NotFoundException
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.stereotype.Service
import java.security.Security

@Service
class UserKeyStoreService(private val userKeyStoreRepository: UserKeyStoreRepository) {
    init {
        Init.init()
        Security.addProvider(BouncyCastleProvider())
    }

    /**
     * Persist the User certificates to DB
     */
    fun save(userKeyStore: UserKeyStore): Long {
        userKeyStoreRepository.saveAndFlush(userKeyStore)
        return userKeyStore.id!!
    }

    fun loadById(id: Long): UserKeyStore {
        try {
            return userKeyStoreRepository.getOne(id)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(id, "UserKeyStore", ex)
        }
    }

    fun deleteById(id: Long) {
        try {
            userKeyStoreRepository.deleteById(id)
        } catch (ex: ObjectRetrievalFailureException) {
            throw NotFoundException(id, "UserKeyStore", ex)
        }
    }

    /*fun loadByUserId(userId:Long):UserKeyStore {
        return userKeyStoreRepository.getKeyStoreByUserId(userId)
    }*/
}