package org.ebics.client.ebicsrestapi.utils

import org.ebics.client.api.EbicsConfiguration
import org.ebics.client.api.bankconnection.permission.BankConnectionAccessType
import org.ebics.client.ebicsrestapi.MockUser
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionFactory
import org.ebics.client.model.EbicsProduct
import org.ebics.client.model.EbicsSession

class EbicsSessionFactoryMockImpl(private val configuration: EbicsConfiguration, private val product: EbicsProduct) : IEbicsSessionFactory {
    override fun getSession(
        userIdPass: UserIdPass,
        bankKeysRequired: Boolean,
        accessType: BankConnectionAccessType
    ): EbicsSession {
        val user = MockUser.createMockUser(1, true)
        return EbicsSession(user, configuration, product, user.keyStore!!.toUserCertMgr("pass1"), null)
    }
}