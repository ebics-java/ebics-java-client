package org.ebics.client.ebicsrestapi.bankconnection.h004

import org.ebics.client.ebicsrestapi.bankconnection.AbstractUploadRequest
import org.ebics.client.order.AttributeType

/**
 * REST request object to initialize EBICS upload transfer
 * @param password password used to load user certificates
 * @param orderType EBICS 2.4/2.5 order type (specification of the order format)
 * @param attributeType EBICS Attribute Type (DZHNN without signature, OZHNN with signature)
 * @param params additional key-value pair params for EBICS request
 */
class UploadRequest(
    password: String,
    val orderType: String? = null,
    val attributeType: AttributeType,
    params: Map<String, String>? = emptyMap(),
) : AbstractUploadRequest(password, params)


