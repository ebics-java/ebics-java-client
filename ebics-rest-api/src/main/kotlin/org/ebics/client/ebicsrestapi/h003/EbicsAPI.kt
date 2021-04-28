package org.ebics.client.ebicsrestapi.h003

import org.ebics.client.ebicsrestapi.EbicsFileModel
import org.ebics.client.keymgmt.h003.KeyManagementImpl
import org.ebics.client.session.Product
import org.springframework.stereotype.Component

@Component
class EbicsAPI(private val ebicsFileModel: EbicsFileModel) {
    private val defaultProduct = Product("EBICS 2.4 H003 REST API Client", "en", null)
    fun sendINI(hostId:String, partnerId:String, userId:String, password:String) {
        val user = ebicsFileModel.loadUser(hostId, partnerId, userId, password)
        val session = ebicsFileModel.createSession(user, defaultProduct)
        KeyManagementImpl(session).sendINI(null)
    }
}