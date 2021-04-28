package org.ebics.client.ebicsrestapi.h003

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController("h003")
class EbicsAPIResource (private val ebicsAPI: EbicsAPI){
    @PostMapping("sendIni")
    fun sendINI(hostId:String, partnerId:String, userId:String, password:String) =
        ebicsAPI.sendINI(hostId, partnerId, userId, password)
}