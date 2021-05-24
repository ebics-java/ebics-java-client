package org.ebics.client.ebicsrestapi

import org.ebics.client.api.user.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("users")
class EbicsUsersResource (
    private val userInfoRepository: UserInfoRepository,
    private val userService: UserService)
{
    @GetMapping("test")
    fun index(): List<Pair<String, String>> = listOf(
            Pair("1", "Hello!"),
            Pair("2", "Bonjour!"),
    )

    @GetMapping("")
    fun listUserId(): List<UserInfo> = userInfoRepository.findAll()

    @GetMapping("{id}")
    fun getUserById(@PathVariable userId: Long): UserInfo = userInfoRepository.getOne(userId)

    @DeleteMapping("{id}")
    fun deleteUserById(@PathVariable userId: Long) = userService.delete(userId)

    @PostMapping("")
    fun createUserInfo(@RequestBody userInfo: UserInfo) = userService.createUserInfo(userInfo)

    @PostMapping("{id}/certificates")
    fun createUserCertificates(@PathVariable userId: Long, password:String) = userService.createUserCertificates(userId, password)
}