package org.ebics.client.ebicsrestapi.bankconnection.h005

import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.bank.cert.BankKeyStoreService
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionServiceImpl
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionFactory
import org.ebics.client.keymgmt.h005.KeyManagementImpl
import org.springframework.stereotype.Component

@Component("EbicsKeyManagementAPIH005")
class EbicsKeyManagementAPI(
    private val userService: BankConnectionServiceImpl,
    private val bankService: BankService,
    private val bankKeyStoreService: BankKeyStoreService,
    private val sessionFactory: IEbicsSessionFactory
) {

    fun sendINI(userIdPass: UserIdPass) {
        val session = sessionFactory.getSession(userIdPass, false)
        KeyManagementImpl(session).sendINI(null)
        //The state of user was changed after INI, must be persisted
        userService.saveUser(session.user as BankConnectionEntity)
    }

    fun sendHIA(userIdPass: UserIdPass) {
        val session = sessionFactory.getSession(userIdPass, false)
        KeyManagementImpl(session).sendHIA(null)
        //The state of user was changed after HIA, must be persisted
        userService.saveUser(session.user as BankConnectionEntity)
    }

    fun sendHPB(userIdPass: UserIdPass) {
        val session = sessionFactory.getSession(userIdPass, false)
        val bankCertManager = KeyManagementImpl(session).sendHPB(userIdPass.password)
        val user = session.user as BankConnectionEntity
        val bankKeyStore = BankKeyStore.fromBankCertMgr(bankCertManager, user.partner.bank)
        bankKeyStoreService.save(bankKeyStore) //BankKeyStore must be saved
        bankService.updateKeyStore(user.partner.bank, bankKeyStore) //BankKeyStore must be added to bank
        userService.saveUser(user) //The state of user was changed after HPB, must be persisted
    }
}