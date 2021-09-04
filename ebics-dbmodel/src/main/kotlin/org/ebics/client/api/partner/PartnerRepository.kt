package org.ebics.client.api.partner

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PartnerRepository : JpaRepository<Partner, Long> {
    @Query("SELECT p FROM Partner p WHERE p.partnerId = ?1 AND p.bank.id = ?2")
    fun getPartnerByEbicsPartnerId(ebicsPartnerId: String, bankId: Long): Partner?
}