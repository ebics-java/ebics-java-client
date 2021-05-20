package org.ebics.client.ebicsrestapi

import org.ebics.client.api.user.*
import org.springframework.web.bind.annotation.*

@RestController
//@RequestMapping("users")
class EbicsUsersResource (
    private val userInfoRepository: UserInfoRepository,
    private val userService: UserService)
{
    @GetMapping("test")
    fun index(): List<Pair<String, String>> = listOf(
            Pair("1", "Hello!"),
            Pair("2", "Bonjour!"),
    )

    @GetMapping()
    fun listUserId(): List<UserInfo> = userInfoRepository.findAll()

    @GetMapping()
    fun getUserById(@RequestParam userId: Long): UserInfo = userInfoRepository.getOne(userId)

    @DeleteMapping()
    fun deleteUserById(@RequestParam userId: Long) = userService.delete(userId)

    @PostMapping("createUserInfo")
    fun createUserInfo(userInfo: UserInfo) = userService.createUserInfo(userInfo)

    @PostMapping("createUser")
    fun createUserWithCertificates(@RequestParam userId: Long, password:String) = userService.createUserWithCertificates(userId, password)
}