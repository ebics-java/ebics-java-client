package org.ebics.client.ebicsrestapi.bankconnection.h003

import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.UserPass
import org.springframework.web.bind.annotation.*

@RestController("EbicsAPIResourceH003")
@RequestMapping("bankconnections/{userId}/H003")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsAPIResource (private val ebicsAPI: EbicsAPI){
    @PostMapping("sendINI")
    fun sendINI(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPI.sendINI(UserIdPass(userId, userPass.password))

    @PostMapping("sendHIA")
    fun sendHIA(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPI.sendHIA(UserIdPass(userId, userPass.password))

    @PostMapping("sendHPB")
    fun sendHPB(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPI.sendHPB(UserIdPass(userId, userPass.password))
}