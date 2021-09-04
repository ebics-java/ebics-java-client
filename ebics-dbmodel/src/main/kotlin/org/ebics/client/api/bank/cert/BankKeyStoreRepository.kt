package org.ebics.client.api.bank.cert

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BankKeyStoreRepository : JpaRepository<BankKeyStore, Long>