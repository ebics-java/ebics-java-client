package org.ebics.client.api.partner

import org.ebics.client.api.bank.BankRepository
import org.springframework.stereotype.Service

@Service
class PartnerService(
    private val partnerRepository: PartnerRepository,
    private val bankRepository: BankRepository
) {
    fun createOrGetPartner(ebicsPartnerId: String, bankId: Long): Partner {
        val existingPartner = partnerRepository.getPartnerByEbicsPartnerId(ebicsPartnerId, bankId)
        return if (existingPartner == null) {
            val bank = bankRepository.getOne(bankId)
            val newPartner = Partner(null, bank, ebicsPartnerId, 0)
            partnerRepository.saveAndFlush(newPartner)
            newPartner
        } else
            existingPartner
    }

    fun addPartner(partner: Partner): Long {
        partnerRepository.saveAndFlush(partner)
        return partner.id!!
    }

    fun getPartnerById(partnerId: Long): Partner = partnerRepository.getOne(partnerId)

    fun deletePartnerById(partnerId: Long) = partnerRepository.deleteById(partnerId)

    fun findAll(): List<Partner> = partnerRepository.findAll()
}