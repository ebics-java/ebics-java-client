package org.ebics.client.api.partner

import org.springframework.stereotype.Service

@Service
class PartnerService(private val partnerRepository: PartnerRepository) {
    fun addPartner(partner: Partner): Long {
        partnerRepository.saveAndFlush(partner)
        return partner.id!!
    }

    fun getPartnerById(partnerId:Long):Partner = partnerRepository.getOne(partnerId)

    fun findAll(): List<Partner> = partnerRepository.findAll()
}