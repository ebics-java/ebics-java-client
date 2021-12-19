package org.ebics.client.ebicsrestapi.bankconnection.session

import org.ebics.client.api.user.UserService
import org.ebics.client.model.EbicsSession
import org.ebics.client.api.user.permission.BankConnectionAccessType
import org.ebics.client.ebicsrestapi.EbicsRestConfiguration
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.model.Product
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.util.*

@Component
class EbicsSessionCache(
    private val userService: UserService,
    private val configuration: EbicsRestConfiguration
) {
    @Cacheable("sessions", key = "#userIdPass")
    fun getSession(
        userIdPass: UserIdPass,
        product: Product,
        bankKeysRequired: Boolean = true,
        accessType: BankConnectionAccessType = BankConnectionAccessType.USE
    ): EbicsSession {
        return createSession(userIdPass, product, bankKeysRequired, accessType)
    }

    private fun createSession(
        userIdPass: UserIdPass,
        product: Product,
        bankKeysRequired: Boolean = true,
        accessType: BankConnectionAccessType = BankConnectionAccessType.USE
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
