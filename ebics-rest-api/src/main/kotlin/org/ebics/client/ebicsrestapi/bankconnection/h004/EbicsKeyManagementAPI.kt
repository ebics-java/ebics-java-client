package org.ebics.client.ebicsrestapi.bankconnection.h004

import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.bank.cert.BankKeyStoreService
import org.ebics.client.api.user.User
import org.ebics.client.api.user.UserServiceImpl
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionCache
import org.ebics.client.keymgmt.h004.KeyManagementImpl
import org.ebics.client.model.Product
import org.springframework.stereotype.Component

@Component("EbicsKeyManagementAPIH004")
class EbicsKeyManagementAPI(
    private val userService: UserServiceImpl,
    private val bankService: BankService,
    private val bankKeyStoreService: BankKeyStoreService,
    private val sessionCache: IEbicsSessionCache
) {
    private val product =
        Product("EBICS 2.5 H004 REST API Client", "en", "org.jto.ebics")

    fun sendINI(userIdPass: UserIdPass) {
        val session = sessionCache.getSession(userIdPass, product, false)
        KeyManagementImpl(session).sendINI(null)
        //The state of user was changed after INI, must be persisted
        userService.saveUser(session.user as User)
    }

    fun sendHIA(userIdPass: UserIdPass) {
        val session = sessionCache.getSession(userIdPass, product, false)
        KeyManagementImpl(session).sendHIA(null)
        //The state of user was changed after HIA, must be persisted
        userService.saveUser(session.user as User)
    }

    fun sendHPB(userIdPass: UserIdPass) {
        val session = sessionCache.getSession(userIdPass, product, false)
        val bankCertManager = KeyManagementImpl(session).sendHPB(userIdPass.password)
        val user = session.user as User
        val bankKeyStore = BankKeyStore.fromBankCertMgr(bankCertManager, user.partner.bank)
        bankKeyStoreService.save(bankKeyStore) //BankKeyStore must be saved
        bankService.updateKeyStore(user.partner.bank, bankKeyStore) //BankKeyStore must be added to bank
        userService.saveUser(user) //The state of user was changed after HPB, must be persisted
    }
}