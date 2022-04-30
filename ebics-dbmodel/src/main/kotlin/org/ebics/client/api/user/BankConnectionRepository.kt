package org.ebics.client.api.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BankConnectionRepository : JpaRepository<BankConnectionEntity, Long>