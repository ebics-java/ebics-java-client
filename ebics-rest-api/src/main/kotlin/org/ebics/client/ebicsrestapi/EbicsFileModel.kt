package org.ebics.client.ebicsrestapi

import org.ebics.client.api.EbicsModel
import org.ebics.client.session.DefaultConfiguration
import org.springframework.stereotype.Component

@Component
class EbicsFileModel {
    private val configuration = DefaultConfiguration()
    private val ebicsModel: EbicsModel = EbicsModel(configuration)

    fun listUserId(): List<String> = ebicsModel.listUserId()
    fun listBankId(): List<String> = ebicsModel.listBankId()
    fun listPartnerId(): List<String> = ebicsModel.listPartnerId()
}