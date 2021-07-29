package org.ebics.client.ebicsrestapi.user

import org.ebics.client.api.user.*
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("users")
@CrossOrigin(origins = ["http://localhost:8080"])
class EbicsUsersResource (
    private val userService: UserService)
{
    @GetMapping("")
    fun listUsers(): List<User> = userService.findUsers()

    @GetMapping("{userId}")
    fun getUserById(@PathVariable userId: Long): User = userService.getUserById(userId)

    @DeleteMapping("{userId}")
    fun deleteUserById(@PathVariable userId: Long) = userService.deleteUser(userId)

    @PostMapping("")
    fun createUser(@RequestBody userPartnerBank: UserPartnerBank):Long =
        userService.createUserAndPartner(userPartnerBank)

    @PutMapping("{userId}")
    fun updateUser(@PathVariable userId:Long, @RequestBody userPartnerBank: UserPartnerBank) =
        userService.updateUserAndPartner(userId, userPartnerBank)

    @PostMapping("{userId}/certificates")
    fun createUserCertificates(@PathVariable userId: Long, @RequestBody userPass: UserPass):Long = userService.createOrUpdateUserCertificates(userId, userPass.password)

    @PostMapping("{userId}/resetStatus")
    fun resetStatus(@PathVariable userId: Long) = userService.resetStatus(userId)
}