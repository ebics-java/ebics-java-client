package org.ebics.client.api.bank.cert

import org.ebics.client.api.getById
import org.springframework.stereotype.Service

@Service
class BankKeyStoreService(private val bankKeyStoreRepository: BankKeyStoreRepository) {
    fun save(bankKeyStore: BankKeyStore): Long {
        bankKeyStoreRepository.saveAndFlush(bankKeyStore)
        return bankKeyStore.id!!
    }

    fun load(bankKeyStoreId:Long): BankKeyStore {
        return bankKeyStoreRepository.getById(bankKeyStoreId, "BankKeyStore")
    }
}