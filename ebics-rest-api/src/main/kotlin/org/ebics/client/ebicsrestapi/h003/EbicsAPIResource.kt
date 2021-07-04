package org.ebics.client.ebicsrestapi.h003

import org.ebics.client.ebicsrestapi.UserIdPass
import org.springframework.web.bind.annotation.*

@RestController()
@RequestMapping("h003")
@CrossOrigin(origins = ["http://localhost:8080"])
class EbicsAPIResource (private val ebicsAPI: EbicsAPI){
    @PostMapping("sendINI")
    fun sendINI(@RequestBody userIdPass:UserIdPass) = ebicsAPI.sendINI(userIdPass)

    @PostMapping("sendHIA")
    fun sendHIA(@RequestBody userIdPass:UserIdPass) = ebicsAPI.sendHIA(userIdPass)
}