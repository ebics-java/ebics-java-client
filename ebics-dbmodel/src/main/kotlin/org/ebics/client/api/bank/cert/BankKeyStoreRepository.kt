package org.ebics.client.api.bank.cert

import org.ebics.client.api.CustomJpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BankKeyStoreRepository : CustomJpaRepository<BankKeyStore, Long>