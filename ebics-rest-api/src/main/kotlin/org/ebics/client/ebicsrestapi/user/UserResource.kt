package org.ebics.client.ebicsrestapi.user

import org.ebics.client.api.security.AuthenticationContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("user")
@CrossOrigin(origins = ["http://localhost:8081"])
class UserResource(@Value("\${build.revision}") val buildVersion: String,
                   @Value("\${build.timestamp}") val buildTimestamp: String) {
    @GetMapping()
    fun user(): UserContext
    {
        with(
            AuthenticationContext.fromSecurityContext()
        ) {
            return UserContext(name, roles.map { it.toString() }, buildVersion, buildTimestamp)
        }
    }
}

