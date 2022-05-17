package org.ebics.client.api.bankconnection

import org.ebics.client.model.EbicsVersion

/**
 * Used as POJO for adding new or updating existing bank connection
 */
data class BankConnection(
    val ebicsVersion: EbicsVersion,
    val userId: String,
    val name: String,
    val partnerId: String,
    val bankId: Long,
    val guestAccess: Boolean,
    val useCertificate: Boolean,
)
