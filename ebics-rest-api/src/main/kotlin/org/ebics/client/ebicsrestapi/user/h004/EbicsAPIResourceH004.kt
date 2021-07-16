package org.ebics.client.ebicsrestapi.user.h004

import org.ebics.client.ebicsrestapi.user.UserIdPass
import org.ebics.client.ebicsrestapi.user.UserPass
import org.springframework.web.bind.annotation.*

@RestController()
@RequestMapping("users/{userId}/H004")
@CrossOrigin(origins = ["http://localhost:8080"])
class EbicsAPIResourceH004 (private val ebicsAPIH004: EbicsAPIH004){
    @PostMapping("sendINI")
    fun sendINI(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPIH004.sendINI(UserIdPass(userId, userPass.password))

    @PostMapping("sendHIA")
    fun sendHIA(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPIH004.sendHIA(UserIdPass(userId, userPass.password))

    @PostMapping("sendHPB")
    fun sendHPB(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPIH004.sendHPB(UserIdPass(userId, userPass.password))
}