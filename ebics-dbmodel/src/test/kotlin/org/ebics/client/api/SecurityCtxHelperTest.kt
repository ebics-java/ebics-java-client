package org.ebics.client.api

import org.ebics.client.api.security.AuthenticationContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class AuthenticationContextTest {
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN", "USER"])
    fun testSecCtxHelper() {
        Assertions.assertEquals("admin", AuthenticationContext.fromSecurityContext().name)
        Assertions.assertEquals(2, AuthenticationContext.fromSecurityContext().roles.size)
    }
}