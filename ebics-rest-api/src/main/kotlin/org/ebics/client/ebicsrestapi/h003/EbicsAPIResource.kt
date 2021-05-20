package org.ebics.client.ebicsrestapi.h003

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController("h003")
class EbicsAPIResource (private val ebicsAPI: EbicsAPI){
    @PostMapping("sendINI")
    fun sendINI(userId:Long, password:String) = ebicsAPI.sendINI(userId, password)
}