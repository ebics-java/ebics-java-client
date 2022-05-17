package org.ebics.client.api.bankconnection

import DbTestContext
import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.model.EbicsVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [DbTestContext::class])
class UserServiceTest(
    @Autowired private val userService: BankConnectionServiceImpl,
    @Autowired private val bankService: BankService,
) {
    @Test
    @WithMockUser(username = "user_xxx", roles = ["USER"])
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