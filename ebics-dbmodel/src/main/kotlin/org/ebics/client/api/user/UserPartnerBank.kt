package org.ebics.client.api.user

import org.ebics.client.model.EbicsVersion

data class UserPartnerBank(
    val ebicsVersion: EbicsVersion,
    val userId: String,
    val name: String,
    val dn: String,
    val partnerId: String,
    val bankId: Long,
    val useCertificate: Boolean,
    val usePassword: Boolean
)
