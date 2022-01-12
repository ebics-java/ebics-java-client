package org.ebics.client.ebicsrestapi

import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.user.BankConnection
import org.ebics.client.api.user.User
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatusEnum
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URL


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EbicsBankConnectionResourceTest (@Autowired private val restTemplate: TestRestTemplate) {
    @Test
    fun greetingShouldReturnDefaultMessage() {
        assertThat(restTemplate.getForObject("/users/test", String::class.java))
            .contains("Hello", "Bonjour")
    }

    @Test
    fun addAndGetBankConnection() {
        //Save bank
        val bank = Bank(null, URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH", null)
        val requestBank: HttpEntity<Bank> = HttpEntity(bank)
        val bankId = restTemplate.postForObject("/banks", requestBank, Long::class.java)

        //Save user + Partner
        val userInfo = BankConnection( EbicsVersion.H005, "CHT10001", "JT", "CH100001", bankId, true, true)
        val request: HttpEntity<BankConnection> = HttpEntity(userInfo)
        val targetUrl: URI = UriComponentsBuilder.fromUriString(restTemplate.rootUri)
            .path("/users") // Add path
            //.queryParam("ebicsPartnerId", "CH100001")
            //.queryParam("bankId", bankId)
            .build() // Build the URL
            .encode() // Encode any URI items that need to be encoded
            .toUri()

        val userId = restTemplate.postForObject(targetUrl, request, Long::class.java)
        assertThat(userId).isNotNull

        val user: User = restTemplate.getForObject ("/users/{userId}", User::class.java, userId)
        assertThat(user).isNotNull
        with (user) {
            assertThat(name).isEqualTo("JT")
            assertThat(dn).startsWith("cn=jt")
            assertThat(partner.partnerId).isEqualTo("CH100001")
            assertThat(bank.name).isEqualTo("UBS-PROD-CH")
            assertThat(userStatus).isEqualTo(EbicsUserStatusEnum.CREATED)
            assertThat(ebicsVersion).isEqualTo(EbicsVersion.H005)
            assertThat(useCertificate).isEqualTo(true)
            assertThat(usePassword).isEqualTo(true)
        }
    }

    class UserList : MutableList<User> by ArrayList()
}