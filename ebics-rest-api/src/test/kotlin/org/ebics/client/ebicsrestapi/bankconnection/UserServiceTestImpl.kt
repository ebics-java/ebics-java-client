package org.ebics.client.ebicsrestapi.bankconnection

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.user.BankConnection
import org.ebics.client.api.user.User
import org.ebics.client.api.user.UserService
import org.ebics.client.api.user.cert.UserKeyStore
import org.ebics.client.api.user.permission.BankConnectionAccessType
import org.ebics.client.certificate.BankCertificateManager
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.model.EbicsVersion
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.net.URL

@Primary
@Service
class UserServiceTestImpl : UserService {

    private fun createMockUser(userId: Long, bankCerts: Boolean = true): User {
        val bank = Bank(null, URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH", null)

        if (bankCerts) {
            val key = "b3BlbnNzaC1rZXktdjEAAAAACmFlczI1Ni1jdHIAAAAGYmNyeXB0AAAAGAAAABBubT4Zco\n" +
                    "dVjOiI1CkzPDr/AAAAEAAAAAEAAAGXAAAAB3NzaC1yc2EAAAADAQABAAABgQDLtT0Cz9Bh\n" +
                    "EqtjII0kacPGOZX7UNsmNesARxK36sXxqp7Jwur1/MSWjFJd3Y0v22mnxBruBxEZ+bwut0\n" +
                    "hFUix5CiI8Jv2hHm6rUfZxNFDZwkXfcVV1QEmaDfEjB+X4DXYuzHsG408K2E7d3VHV1efL\n" +
                    "I81Rwey/uR2eNOGigCkoOW0XBjcUjlC/GUu4Um7MV5dTCqCRPEOxB2Euj9mcsmdC+0KuIf\n" +
                    "t7zww3VJONS4x1Su4/CPX3eN7z/WmQztnmdSJ1BvrCdLiGFOn6v/ezROFsMfmCgbzSfFvl\n"
            val exponent = "fiNjzkY"
            val bankCertMgr = BankCertificateManager.createFromPubKeyExponentAndModulus(
                exponent.toByteArray(),
                key.toByteArray(),
                exponent.toByteArray(),
                key.toByteArray()
            )
            val bankKeyStore = BankKeyStore.fromBankCertMgr(bankCertMgr, bank)
            bank.keyStore = bankKeyStore
        }
        val partner = Partner(null, bank, "CH10000$userId", 0)
        val user = User(
            userId,
            EbicsVersion.H005,
            "CHT10001",
            "UserId:$userId",
            "o=jto",
            keyStore = null,
            partner = partner,
            usePassword = false,
            useCertificate = true,
            creator = "Creator:$userId",
            guestAccess = true
        )
        val certMgr = UserCertificateManager.create("o=jto")
        val keyStore = UserKeyStore.fromUserCertMgr(user, certMgr, "pass$userId")
        user.keyStore = keyStore
        return user
    }

    private val mockUsers = mapOf(
        1L to createMockUser(1, true),
        2L to createMockUser(2, false)
    )

    override fun findUsers(permission: BankConnectionAccessType): List<User> {
        TODO("Not yet implemented")
    }

    override fun getUserById(userId: Long, permission: BankConnectionAccessType): User {
        return mockUsers[userId]!!
    }

    override fun saveUser(bankConnection: User): Long {
        TODO("Not yet implemented")
    }

    override fun createUserAndPartner(bankConnection: BankConnection): Long {
        TODO("Not yet implemented")
    }

    override fun updateUserAndPartner(id: Long, bankConnection: BankConnection): Long {
        TODO("Not yet implemented")
    }

    override fun deleteUser(userId: Long) {
        TODO("Not yet implemented")
    }

    override fun resetStatus(userId: Long) {
        TODO("Not yet implemented")
    }
}