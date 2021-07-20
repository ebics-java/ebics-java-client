package org.ebics.client.ebicsrestapi.user.h004

import org.ebics.client.ebicsrestapi.user.*
import org.springframework.web.bind.annotation.*

@RestController("EbicsAPIResourceH004")
@RequestMapping("users/{userId}/H004")
@CrossOrigin(origins = ["http://localhost:8080"])
class EbicsAPIResource (private val ebicsAPI: EbicsAPI){
    @PostMapping("sendINI")
    fun sendINI(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPI.sendINI(UserIdPass(userId, userPass.password))

    @PostMapping("sendHIA")
    fun sendHIA(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPI.sendHIA(UserIdPass(userId, userPass.password))

    @PostMapping("sendHPB")
    fun sendHPB(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPI.sendHPB(UserIdPass(userId, userPass.password))

    @PostMapping("upload")
    fun initiateUpload(@PathVariable userId: Long, @RequestBody uploadInitRequest: UploadInitRequest): UploadInitResponse = ebicsAPI.initFileUpload(userId, uploadInitRequest)

    @PostMapping("upload/{transferId}/sendSegment")
    fun uploadFileSegment(@PathVariable userId: Long, @PathVariable transferId:String, @RequestBody uploadSegmentRequest: UploadSegmentRequest): UploadSegmentResponse =
        ebicsAPI.uploadFileSegment(userId, transferId, uploadSegmentRequest)
}