package org.ebics.client.ebicsrestapi

import org.ebics.client.model.Product
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class EbicsProduct(
    @Value("\${ebics.product.name:EBICS REST API Client}")
    override val name: String,

    @Value("\${ebics.product.language:en}")
    override val language: String,

    @Value("\${ebics.product.instituteID:org.jto.ebics}")
    override val instituteID: String?
) : Product