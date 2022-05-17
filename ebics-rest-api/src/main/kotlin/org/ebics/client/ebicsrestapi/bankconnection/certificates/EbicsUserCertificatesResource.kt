package org.ebics.client.ebicsrestapi.bankconnection.certificates

import org.ebics.client.api.bankconnection.cert.CertRequest
import org.ebics.client.api.bankconnection.cert.UserCertificateService
import org.ebics.client.ebicsrestapi.bankconnection.UserPass
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("bankconnections/{userId}/certificates")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsUserCertificatesResource (
    private val certificateService: UserCertificateService)
{
    @PostMapping("")
    fun createUserCertificates(@PathVariable userId: Long, @RequestBody cert: CertRequest):Long = certificateService.createOrUpdateUserCertificates(userId, cert)

    @PostMapping("letters")
    fun getUserLetters(@PathVariable userId: Long, @RequestBody userPass: UserPass) = certificateService.getUserLetters(userId, userPass.password)
}