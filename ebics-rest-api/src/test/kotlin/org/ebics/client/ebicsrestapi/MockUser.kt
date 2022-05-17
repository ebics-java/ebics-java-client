package org.ebics.client.ebicsrestapi

import org.apache.xml.security.Init
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.cert.UserKeyStore
import org.ebics.client.certificate.BankCertificateManager
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.model.EbicsVersion
import java.net.URL
import java.security.Security

class MockUser {
    companion object {
        init {
            Init.init()
            Security.addProvider(BouncyCastleProvider())
        }

        fun createMockUser(userId: Long, bankCerts: Boolean = true): BankConnectionEntity {
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
            val user = BankConnectionEntity(
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
    }
}