package org.ebics.client.ebicsrestapi

import org.ebics.client.api.user.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("users")
@CrossOrigin(origins = ["http://localhost:8080"])
class EbicsUsersResource (
    private val userInfoRepository: UserInfoRepository,
    private val userService: UserService)
{
    @GetMapping("")
    fun listUsers(): List<UserInfo> = userInfoRepository.findAll()

    @GetMapping("{id}")
    fun getUserById(@PathVariable userId: Long): UserInfo = userInfoRepository.getOne(userId)

    @DeleteMapping("{id}")
    fun deleteUserById(@PathVariable userId: Long) = userService.delete(userId)

    @PostMapping("")
    fun createUserInfo(@RequestBody userInfo: UserInfo):Long = userService.createUserInfo(userInfo)

    @PostMapping("{id}/certificates")
    fun createUserCertificates(@PathVariable userId: Long, password:String) = userService.createUserCertificates(userId, password)
}