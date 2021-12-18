package org.ebics.client.api.user

import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.user.cert.UserKeyStoreService
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.model.EbicsVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
@AutoConfigurationPackage(basePackages = ["org.ebics.client.api.*"])
@ContextConfiguration(classes = [BankService::class, UserKeyStoreService::class, PartnerService::class, UserServiceImpl::class])
class UserServiceTest(
    @Autowired private val userService: UserServiceImpl,
    @Autowired private val bankService: BankService,
) {
    @Test
    fun createAndGetUser() {
        val bank = BankData(  URL("https://ebics.ubs.com/ebicsweb/ebicsweb"),  "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bank)
        val userInfo = BankConnection(EbicsVersion.H004, "CHT10001", "Jan",  "CH100001", bankId, false, false)
        val userId = userService.createUserAndPartner(userInfo)
        with( userService.getUserById(userId) ) {
            assertThat( name ).isEqualTo( userInfo.name )
            assertThat( partner.bank.bankURL ).isEqualTo( bank.bankURL )
            assertThat( ebicsVersion ).isEqualTo( userInfo.ebicsVersion )
            assertThat( partner.partnerId ).isEqualTo("CH100001")
        }
    }
}