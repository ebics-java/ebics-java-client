package org.ebics.client.ebicsrestapi

import org.ebics.client.api.EbicsConfiguration
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsProduct
import org.ebics.client.model.EbicsSession

class MockSession {
    companion object {
        fun getSession(userId: Long, bankCerts: Boolean = true, prod: EbicsProduct, configuration: EbicsConfiguration): EbicsSession {
            val user = MockUser.createMockUser(userId, bankCerts)
            return getSession(user, prod, configuration, bankCerts)
        }

        fun getSession(user: BankConnectionEntity, prod: EbicsProduct, configuration: EbicsConfiguration, bankCerts: Boolean = true): EbicsSession {
            return EbicsSession(
                user,
                configuration,
                prod,
                user.keyStore!!.toUserCertMgr("pass${user.id}"),
                if (bankCerts) user.partner.bank.keyStore!!.toBankCertMgr() else null)
        }
    }
}