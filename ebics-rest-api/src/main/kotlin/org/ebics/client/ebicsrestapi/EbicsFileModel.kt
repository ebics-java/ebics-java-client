package org.ebics.client.ebicsrestapi

import org.ebics.client.api.EbicsModel
import org.ebics.client.api.User
import org.ebics.client.session.DefaultConfiguration
import org.ebics.client.session.EbicsSession
import org.ebics.client.session.Product
import org.springframework.stereotype.Component

@Component
class EbicsFileModel {
    private val configuration = DefaultConfiguration()
    private val ebicsModel: EbicsModel = EbicsModel(configuration)

    fun listUserId(): List<String> = ebicsModel.listUserId()
    fun listBankId(): List<String> = ebicsModel.listBankId()
    fun listPartnerId(): List<String> = ebicsModel.listPartnerId()

    fun loadUser(hostId: String, partnerId: String, userId: String, password: String):User =
        ebicsModel.loadUser(hostId, partnerId, userId) { password.toCharArray() }

    fun createSession(user: User, defaultProduct: Product): EbicsSession =
        ebicsModel.createSession(user, defaultProduct)
}