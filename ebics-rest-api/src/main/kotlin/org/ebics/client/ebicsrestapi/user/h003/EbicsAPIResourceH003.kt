package org.ebics.client.ebicsrestapi.user.h003

import org.ebics.client.ebicsrestapi.user.UserIdPass
import org.ebics.client.ebicsrestapi.user.UserPass
import org.springframework.web.bind.annotation.*

@RestController()
@RequestMapping("users/{userId}/H003")
@CrossOrigin(origins = ["http://localhost:8080"])
class EbicsAPIResourceH003 (private val ebicsAPIH003: EbicsAPIH003){
    @PostMapping("sendINI")
    fun sendINI(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPIH003.sendINI(UserIdPass(userId, userPass.password))

    @PostMapping("sendHIA")
    fun sendHIA(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPIH003.sendHIA(UserIdPass(userId, userPass.password))

    @PostMapping("sendHPB")
    fun sendHPB(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPIH003.sendHPB(UserIdPass(userId, userPass.password))
}