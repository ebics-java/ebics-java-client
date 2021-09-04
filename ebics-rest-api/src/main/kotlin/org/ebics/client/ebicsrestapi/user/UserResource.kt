package org.ebics.client.ebicsrestapi.user

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.user.SecurityCtxHelper
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("user")
@CrossOrigin(origins = ["http://localhost:8081"])
class UserResource {
    @GetMapping()
    fun user(): UserRoles
    {
        with(
            SecurityCtxHelper.getAuthentication()
        ) {
            return UserRoles(name, authorities.map { it.toString() })
        }
    }
}

