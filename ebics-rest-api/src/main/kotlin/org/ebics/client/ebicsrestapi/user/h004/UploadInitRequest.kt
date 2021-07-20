package org.ebics.client.ebicsrestapi.user.h004

import org.ebics.client.ebicsrestapi.user.AbstractUploadInitRequest
import org.ebics.client.order.AttributeType
import org.ebics.client.order.EbicsService
import org.ebics.client.order.h004.EbicsUploadOrder

/**
 * REST request object to initialize EBICS upload transfer
 * @param password password used to load user certificates
 * @param orderType EBICS 2.4/2.5 order type (specification of the order format)
 * @param attributeType EBICS Attribute Type (DZHNN without signature, OZHNN with signature)
 * @param params additional key-value pair params for EBICS request
 * @param sha256digest SHA256 of the whole input file, in HEX form (NO BASE64)
 * @param fileSizeBytes total number bytes of input file, used for segmentation
 */
class UploadInitRequest(
    password: String,
    val orderType: String? = null,
    val attributeType: AttributeType,
    params: Map<String, String>,
    sha256digest: String,
    fileSizeBytes: Long,
) : AbstractUploadInitRequest(password, params, sha256digest, fileSizeBytes)


