package org.ebics.client.ebicsrestapi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class EbicsFileModelResource (private val ebicsFileModel: EbicsFileModel) {
    @GetMapping
    fun index(): List<Pair<String, String>> = listOf(
            Pair("1", "Hello!"),
            Pair("2", "Bonjour!"),
    )

    @GetMapping("users")
    fun listUserId(): List<String> = ebicsFileModel.listUserId()

    @GetMapping("partners")
    fun listPartnerId(): List<String> = ebicsFileModel.listPartnerId()

    @GetMapping("banks")
    fun listBankIds(): List<String> = ebicsFileModel.listBankId()
}