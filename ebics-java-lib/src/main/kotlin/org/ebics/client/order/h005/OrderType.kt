package org.ebics.client.order.h005

import org.ebics.client.order.AuthorisationLevel
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.EbicsService
import java.math.BigInteger

class OrderType (
    val adminOrderType: EbicsAdminOrderType,
    val service: EbicsService?,
    val description: String?,
    val authorizationLevel: AuthorisationLevel?,
    val numSigRequired: BigInteger?,
)