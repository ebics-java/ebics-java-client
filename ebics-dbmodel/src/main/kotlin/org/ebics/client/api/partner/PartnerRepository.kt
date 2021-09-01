package org.ebics.client.api.partner

import org.ebics.client.api.CustomJpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PartnerRepository : CustomJpaRepository<Partner, Long> {
    @Query("SELECT p FROM Partner p WHERE p.partnerId = ?1 AND p.bank.id = ?2")
    fun getPartnerByEbicsPartnerId(ebicsPartnerId: String, bankId: Long): Partner?
}