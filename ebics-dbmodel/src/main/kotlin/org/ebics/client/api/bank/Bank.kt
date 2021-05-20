package org.ebics.client.api.bank

import org.ebics.client.api.EbicsBank
import java.net.URL
import java.security.interfaces.RSAPublicKey
import javax.persistence.Entity

@Entity
data class Bank(
    override val bankURL: URL,
    override val useCertificate: Boolean,
    override var e002Digest: ByteArray?,
    override var x002Digest: ByteArray?,
    override var e002Key: RSAPublicKey?,
    override var x002Key: RSAPublicKey?,
    override val hostId: String,
    override val name: String
) : EbicsBank
