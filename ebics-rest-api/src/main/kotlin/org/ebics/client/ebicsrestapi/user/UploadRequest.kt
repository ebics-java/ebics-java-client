package org.ebics.client.ebicsrestapi.user

/**
 * REST request object to initialize EBICS upload transfer
 * @param password password used to load user certificates
 * @param params additional key-value pair params for EBICS request
 * @param sha256digest SHA256 of the whole input file, in HEX form (NO BASE64)
 * @param fileSizeBytes total number bytes of input file, used for segmentation
 */
abstract class AbstractUploadInitRequest(
    val password: String,
    val params: Map<String, String>,
    val sha256digest: String,
    val fileSizeBytes: Long,
)

/**
 * REST response object from initialize EBICS upload transfer
 * @param transferId transfer id created, will be used to transfer data segments using UploadSegmentRequest
 * @param segmentSize size of segment in Bytes.
 *      All segments except last one must have exactly this size.
 *      The last segment size is less or equal to segmentSize
 *      (each segment has maximum 1 MiB = 1,048,576 Bytes raw data)
 */
data class UploadInitResponse (
    val transferId: String,
    val segmentSize: Int,
    val orderNumber: String,
)

/**
 * REST request object to upload one segment
 * @param password used to load user certificates
 * @param segmentBytesBase64 BASE64 coded content of the segment data
 */
data class UploadSegmentRequest(
    val password: String,
    val segmentBytesBase64: String,
)

class UploadSegmentResponse(

)