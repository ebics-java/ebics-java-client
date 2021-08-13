package org.ebics.client.ebicsrestapi.bankconnection.h004

import org.ebics.client.ebicsrestapi.bankconnection.*
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController("EbicsAPIResourceH004")
@RequestMapping("bankconnections/{userId}/H004")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsAPIResource (private val ebicsAPI: EbicsAPI){
    @PostMapping("sendINI")
    fun sendINI(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPI.sendINI(UserIdPass(userId, userPass.password))

    @PostMapping("sendHIA")
    fun sendHIA(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPI.sendHIA(UserIdPass(userId, userPass.password))

    @PostMapping("sendHPB")
    fun sendHPB(@PathVariable userId:Long, @RequestBody userPass: UserPass) = ebicsAPI.sendHPB(UserIdPass(userId, userPass.password))

    @PostMapping("upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(@PathVariable userId: Long, @RequestPart uploadRequest: UploadRequest, @RequestPart uploadFile: MultipartFile): UploadResponse = ebicsAPI.uploadFile(userId, uploadRequest, uploadFile)

    @GetMapping("download")
    fun downloadFile(@PathVariable userId: Long, @RequestBody downloadRequest: DownloadRequest): ResponseEntity<Resource> = ebicsAPI.downloadFile(userId, downloadRequest)
}