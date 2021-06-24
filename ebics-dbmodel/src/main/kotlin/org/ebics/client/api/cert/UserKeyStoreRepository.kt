package org.ebics.client.api.cert

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserKeyStoreRepository : JpaRepository<UserKeyStore, Long> {
    @Query("SELECT ks FROM UserKeyStore ks WHERE ks.user.id = ?1")
    fun getKeyStoreByUserId(userId: Long):UserKeyStore
}