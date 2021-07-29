package org.ebics.client.order

/**
 * Initialize EBICS upload order
 * @param adminOrderType the EBICS admin order type
 * @param signatureFlag the signature flag (true = OZHNN, false = DZHNN)
 * @param params the additional key-value parameters for upload
 */
abstract class AbstractEbicsUploadOrder(
    adminOrderType: EbicsAdminOrderType,
    params: Map<String, String>
) :
    EbicsOrder(adminOrderType, params)