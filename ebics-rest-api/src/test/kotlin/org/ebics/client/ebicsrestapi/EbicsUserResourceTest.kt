package org.ebics.client.ebicsrestapi

import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.user.UserInfo
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EbicsUserResourceTest (@Autowired private val restTemplate: TestRestTemplate) {
    @Test
    fun greetingShouldReturnDefaultMessage() {
        assertThat(restTemplate.getForObject("/users/test", String::class.java))
            .contains("Hello", "Bonjour")
    }

    @Test
    fun addGetUser() {
        val user = UserInfo(null, EbicsVersion.H005, "CHT10001", "JT", "cn=JT,org=com", EbicsUserStatus(), null)
        val request: HttpEntity<UserInfo> = HttpEntity(user)
        val userId = restTemplate.postForObject("/users", request, String::class.java)
        assertThat(userId).isEqualTo("1")
    }
}