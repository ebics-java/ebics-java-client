package org.ebics.client.api.partner

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PartnerRepository : JpaRepository<Partner, Long>