package org.ebics.client.ebicsrestapi.bank

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.versions.VersionSupport
import org.ebics.client.api.bank.versions.VersionSupportService
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
import org.ebics.client.model.EbicsVersion
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import java.net.URL

@Configuration
@Lazy
class EbicsBankAPITestContext {
    @MockkBean
    private lateinit var configuration: EbicsRestConfiguration

    @Bean
    fun versionSupportService() = mockk<VersionSupportService>()

    @Bean
    fun bankService() = mockk<BankService>().also { bankService ->
        every {
            bankService.getBankById(1)
        } returns
                Bank(1, URL("https://test.com"), "id1", "name", null)

        val fakeBank = Bank(
            2,
            URL("https://test2.com"),
            "id2",
            "name",
            null
        )
        every {
            bankService.getBankById(2)
        } returns
                Bank(
                    2,
                    URL("https://test2.com"),
                    "id2",
                    "name",
                    null,
                    ebicsVersions = listOf(
                        VersionSupport(EbicsVersion.H005, true, true, true, true, fakeBank),
                        VersionSupport(EbicsVersion.H004, true, true, false, false, fakeBank))
                )
    }

    @Bean
    fun ebicsBankApi() = EbicsBankAPI(configuration, bankService(), versionSupportService())
}