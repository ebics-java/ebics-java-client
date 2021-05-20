package org.ebics.client.api.partner

import org.ebics.client.api.EbicsPartner
import org.ebics.client.api.bank.Bank
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class Partner(

    @ManyToOne(optional = false)
    @JoinColumn(name="PARTNER_ID")
    override val bank: Bank,

    override val partnerId: String,
    override var orderId: Int) : EbicsPartner
