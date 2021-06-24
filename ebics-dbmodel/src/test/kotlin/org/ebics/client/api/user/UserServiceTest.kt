package org.ebics.client.api.user

import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.cert.UserKeyStoreService
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatusEnum
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
@ContextConfiguration(classes = [BankService::class, UserKeyStoreService::class, PartnerService::class, UserService::class])
class UserServiceTest(
    @Autowired private val userService: UserService,
    @Autowired private val bankService: BankService,
) {
    @Test
    fun createAndGetUser() {
        val bank = Bank(null, URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), true, "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bank)
        val userInfo = UserInfo(EbicsVersion.H004, "CHT10001", "Jan", "org=jto", EbicsUserStatusEnum.CREATED)
        val userId = userService.createUser(userInfo, "CH100001", bankId)
        with( userService.getUserById(userId) ) {
            assertThat( name ).isEqualTo( userInfo.name )
            assertThat( dn ).isEqualTo( userInfo.dn )
            assertThat( partner.bank.bankURL ).isEqualTo( bank.bankURL )
            assertThat( ebicsVersion ).isEqualTo( userInfo.ebicsVersion )
            assertThat( partner.partnerId ).isEqualTo("CH100001")
        }
    }
}