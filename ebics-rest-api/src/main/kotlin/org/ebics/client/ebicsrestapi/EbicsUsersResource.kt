package org.ebics.client.ebicsrestapi

import org.ebics.client.api.EbicsUserInfo
import org.ebics.client.api.user.*
import org.springframework.web.bind.annotation.*

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
    fun createUser(@RequestBody userInfo: UserInfo, @RequestParam ebicsPartnerId:String, @RequestParam bankId:Long):Long =
        userService.createUser(userInfo, ebicsPartnerId, bankId)

    @PostMapping("{userId}/certificates")
    fun createUserCertificates(@PathVariable userId: Long, password:String) = userService.createUserCertificates(userId, password)
}