package org.ebics.client.ebicsrestapi

import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.user.UserInfo
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatus
import org.ebics.client.model.user.EbicsUserStatusEnum
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EbicsUserResourceTest (@Autowired private val restTemplate: TestRestTemplate) {
    @Test
    fun greetingShouldReturnDefaultMessage() {
        assertThat(restTemplate.getForObject("/users/test", String::class.java))
            .contains("Hello", "Bonjour")
    }

    @Test
    fun addUserAndGet() {
        val user = UserInfo(null, EbicsVersion.H005, "CHT10001", "JT", "cn=JT,org=com", EbicsUserStatus(), null)
        val request: HttpEntity<UserInfo> = HttpEntity(user)
        val userId = restTemplate.postForObject("/users", request, Long::class.java)
        assertThat(userId).isEqualTo(1)

        val users: UserList = restTemplate.getForObject ("/users", UserList::class.java)
        assertThat(users.size).isEqualTo(1)
        with (users[0] as UserInfo) {
            assertThat(name).isEqualTo("JT")
            assertThat(dn).isEqualTo("cn=JT,org=com")
            assertThat(userStatus.status).isEqualTo(EbicsUserStatusEnum.CREATED)
            assertThat(ebicsVersion).isEqualTo(EbicsVersion.H005)
        }
    }

    class UserList : MutableList<UserInfo> by ArrayList()
}