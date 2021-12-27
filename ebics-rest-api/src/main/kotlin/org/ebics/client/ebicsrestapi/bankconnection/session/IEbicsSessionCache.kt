package org.ebics.client.ebicsrestapi.bankconnection.session

import org.ebics.client.api.user.permission.BankConnectionAccessType
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.Product

interface IEbicsSessionCache {
    fun getSession(
        userIdPass: UserIdPass,
        product: Product,
        bankKeysRequired: Boolean = true,
        accessType: BankConnectionAccessType = BankConnectionAccessType.USE
    ): EbicsSession
}