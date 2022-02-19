package org.ebics.client.certificate

import org.ebics.client.api.UserCertificateManager
import java.security.PrivateKey
import java.security.cert.X509Certificate

class EbicsUserCertificates(
    override val a005Certificate: X509Certificate,
    override val x002Certificate: X509Certificate,
    override val e002Certificate: X509Certificate,
    override val a005PrivateKey: PrivateKey,
    override val x002PrivateKey: PrivateKey,
    override val e002PrivateKey: PrivateKey
) : UserCertificateManager {
    override fun equals(other: Any?): Boolean {
        return if (other is EbicsUserCertificates) {
                    x002Certificate.encoded.contentEquals(other.x002Certificate.encoded) &&
                    e002Certificate.encoded.contentEquals(other.e002Certificate.encoded) &&
                    a005Certificate.encoded.contentEquals(other.a005Certificate.encoded) &&
                    x002PrivateKey.encoded.contentEquals(other.x002PrivateKey.encoded) &&
                    e002PrivateKey.encoded.contentEquals(other.e002PrivateKey.encoded) &&
                    a005PrivateKey.encoded.contentEquals(other.a005PrivateKey.encoded)
        } else false
    }
}