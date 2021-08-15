package org.ebics.client.api.user.cert

class CertificateLetters(
    val signature: Letter,
    val encryption: Letter,
    val authentication: Letter,
)

data class Letter(val letterText: String, val hash: String)