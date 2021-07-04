package org.ebics.client.api.bank

import org.ebics.client.api.NotFoundException
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.stereotype.Service

@Service
class BankService (private val bankRepository: BankRepository) {
    fun findBanks() : List<Bank> = bankRepository.findAll()
    fun createBank(bank: Bank): Long {
        require(bank.id == null) {"This method cant be used for updating of existing bank, provide bank object without id"}
        bankRepository.saveAndFlush(bank)
        return bank.id!!
    }
    fun updateBankById(bankId: Long, bank: Bank) {
        when(bankRepository.findById(bankId).isPresent) {
            true -> bankRepository.save(bank)
            false -> throw NotFoundException(bankId, "bank", null)
        }
    }
    fun deleteBankById(bankId: Long) = bankRepository.deleteById(bankId)
    fun getBankById(bankId: Long): Bank {
        try {
            return bankRepository.getOne(bankId)
        } catch (e: ObjectRetrievalFailureException) {
            throw NotFoundException(bankId, "bank", e)
        }
    }
}