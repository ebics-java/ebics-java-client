package org.ebics.client.ebicsrestapi

import org.ebics.client.model.EbicsProduct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class EbicsProductConfiguration(
    @Value("\${ebics.product.name:EBICS Web Client}")
    val productName: String,

    @Value("\${ebics.product.language:en}")
    override val language: String,

    @Value("\${ebics.product.instituteID:org.jto.ebics}")
    override val instituteID: String?,

    @Value("\${build.revision}")
    val buildVersion: String,
) : EbicsProduct {
    override val name: String
        get() = "$productName v$buildVersion"
}