package org.ebics.client.api.user

import org.ebics.client.model.EbicsVersion

data class BankConnection(
    val ebicsVersion: EbicsVersion,
    val userId: String,
    val name: String,
    val dn: String,
    val partnerId: String,
    val bankId: Long,
    val usePassword: Boolean,
    val guestAccess: Boolean,
)
