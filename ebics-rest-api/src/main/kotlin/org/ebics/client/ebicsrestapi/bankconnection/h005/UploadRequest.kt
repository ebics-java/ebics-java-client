package org.ebics.client.ebicsrestapi.bankconnection.h005

import org.ebics.client.ebicsrestapi.bankconnection.AbstractUploadRequest
import org.ebics.client.order.EbicsService

/**
 * REST request object to initialize EBICS upload transfer
 * @param password password used to load user certificates
 * @param orderService EBICS 3.0 BTF specification of the order format
 * @param signatureFlag if the signature flag is used
 * @param fileName the filename to be uploaded
 * @param params additional key-value pair params for EBICS request
 * @param sha256digest SHA256 of the whole input file, in HEX form (NO BASE64)
 * @param fileSizeBytes total number bytes of input file, used for segmentation
 */
class UploadRequest(
    password: String,
    val orderService: EbicsService,
    val signatureFlag: Boolean,
    val edsFlag: Boolean,
    val fileName: String,
    params: Map<String, String>? = emptyMap(),
) : AbstractUploadRequest(password, params)


