package org.ebics.client.ebicsrestapi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UsersResource (private val ebicsFileModel: EbicsFileModel) {
    @GetMapping
    fun index(): List<Pair<String, String>> = listOf(
            Pair("1", "Hello!"),
            Pair("2", "Bonjour!"),
    )

    @GetMapping("listUserId")
    fun listUserId(): List<String> = ebicsFileModel.listUserId()

    @GetMapping("listPartnerId")
    fun listPartnerId(): List<String> = ebicsFileModel.listPartnerId()

    @GetMapping("listBankId")
    fun listBankIds(): List<String> = ebicsFileModel.listBankId()
}