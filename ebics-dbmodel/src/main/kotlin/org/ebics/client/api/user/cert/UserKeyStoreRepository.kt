package org.ebics.client.api.user.cert

import org.ebics.client.api.CustomJpaRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserKeyStoreRepository : CustomJpaRepository<UserKeyStore, Long> {
    @Query("SELECT ks FROM UserKeyStore ks WHERE ks.user.id = ?1")
    fun getKeyStoreByUserId(userId: Long):UserKeyStore
}