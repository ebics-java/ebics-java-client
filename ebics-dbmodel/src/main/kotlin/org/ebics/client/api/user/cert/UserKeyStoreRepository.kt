package org.ebics.client.api.user.cert

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserKeyStoreRepository : JpaRepository<UserKeyStore, Long> {
    fun getKeyStoreByUserId(userId: Long):UserKeyStore
}