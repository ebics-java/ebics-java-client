package org.ebics.client.order.h004

import org.ebics.client.order.AuthorisationLevel
import org.ebics.client.order.EbicsAdminOrderType
import java.math.BigInteger

class OrderType (
    val adminOrderType: EbicsAdminOrderType?,
    val orderType: String?,
    val transferType: TransferType?,
    val description: String?,
    val authorizationLevel: AuthorisationLevel?,
    val numSigRequired: BigInteger?,
)