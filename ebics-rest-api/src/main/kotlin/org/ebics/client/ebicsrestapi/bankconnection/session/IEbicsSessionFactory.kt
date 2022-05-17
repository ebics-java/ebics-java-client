package org.ebics.client.ebicsrestapi.bankconnection.session

import org.ebics.client.api.bankconnection.permission.BankConnectionAccessType
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.model.EbicsSession

interface IEbicsSessionFactory {
    fun getSession(
        userIdPass: UserIdPass,
        bankKeysRequired: Boolean = true,
        accessType: BankConnectionAccessType = BankConnectionAccessType.USE
    ): EbicsSession
}