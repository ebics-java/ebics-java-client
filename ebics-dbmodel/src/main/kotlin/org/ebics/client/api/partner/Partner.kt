package org.ebics.client.api.partner

import org.ebics.client.api.EbicsPartner
import org.ebics.client.api.bank.Bank
import javax.persistence.*

@Entity
data class Partner(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    @ManyToOne(optional = false)
    override val bank: Bank,

    override val partnerId: String,
    override var orderId: Int) : EbicsPartner
