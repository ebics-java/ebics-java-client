package org.ebics.client.ebicsrestapi.bankconnection

/**
 * REST request object to make EBICS upload transfer
 * @param password password used to load user certificates
 * @param params additional key-value pair params for EBICS request
 */
abstract class AbstractUploadRequest(
    val password: String,
    val params: Map<String, String>? = emptyMap(),
)

/**
 * REST response object from initialize EBICS upload transfer
 * @param orderNumber the EBICS order number
 */
data class UploadResponse (
    val orderNumber: String,
)