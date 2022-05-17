package org.ebics.client.ebicsrestapi.bankconnection.session

import org.ebics.client.api.bankconnection.BankConnectionService
import org.ebics.client.api.bankconnection.permission.BankConnectionAccessType
import org.ebics.client.ebicsrestapi.EbicsProductConfiguration
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
import org.ebics.client.model.EbicsProduct
import org.ebics.client.model.EbicsSession
import org.springframework.stereotype.Component

@Component
class EbicsSessionFactory(
    private val userService: BankConnectionService,
    private val configuration: EbicsRestConfiguration,
    private val product: EbicsProductConfiguration,
) : IEbicsSessionFactory {
    override fun getSession(
        userIdPass: UserIdPass,
        bankKeysRequired: Boolean,
        accessType: BankConnectionAccessType
    ): EbicsSession {
        return createSession(userIdPass, product, bankKeysRequired, accessType)
    }

    private fun createSession(
        userIdPass: UserIdPass,
        product: EbicsProduct,
        bankKeysRequired: Boolean,
        accessType: BankConnectionAccessType
    ): EbicsSession {
        val user = userService.getUserById(userIdPass.id, accessType)
        with(requireNotNull(user.keyStore) { "User certificates must be initialized in order to create EBICS session" }) {
            val manager = toUserCertMgr(userIdPass.password)
            val bankCertManager = if (bankKeysRequired) {
                with(requireNotNull(user.partner.bank.keyStore) { "Bank certificates must be first initialized" }) {
                    toBankCertMgr()
                }
            } else null
            return EbicsSession(user, configuration, product, manager, bankCertManager)
        }
    }
}
