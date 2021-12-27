package org.ebics.client.ebicsrestapi

import org.ebics.client.api.Configuration
import org.ebics.client.api.user.User
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.Product

class MockSession {
    companion object {
        fun getSession(userId: Long, bankCerts: Boolean = true, prod: Product, configuration: Configuration): EbicsSession {
            val user = MockUser.createMockUser(userId, bankCerts)
            return getSession(user, prod, configuration, bankCerts)
        }

        fun getSession(user: User, prod: Product, configuration: Configuration, bankCerts: Boolean = true): EbicsSession {
            return EbicsSession(
                user,
                configuration,
                prod,
                user.keyStore!!.toUserCertMgr("pass${user.id}"),
                if (bankCerts) user.partner.bank.keyStore!!.toBankCertMgr() else null)
        }
    }
}