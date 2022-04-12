package org.ebics.client.certificate

import org.ebics.client.api.UserCertificateManager
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*

class EbicsUserCertificates(
    override val a005Certificate: X509Certificate,
    override val x002Certificate: X509Certificate,
    override val e002Certificate: X509Certificate,
    override val a005PrivateKey: PrivateKey,
    override val x002PrivateKey: PrivateKey,
    override val e002PrivateKey: PrivateKey
) : UserCertificateManager {
    private class CertKeyPair(val certificate: X509Certificate, val privateKey: PrivateKey)

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

    fun save(manager: KeyStoreManager, aliasPrefix: String) {
        with(manager) {
            setKeyEntry("$aliasPrefix-A005", a005PrivateKey, a005Certificate)
            setKeyEntry("$aliasPrefix-X002", x002PrivateKey, x002Certificate)
            setKeyEntry("$aliasPrefix-E002", e002PrivateKey, e002Certificate)
        }
    }

    companion object {
        fun load(manager: KeyStoreManager, aliasPrefix: String): EbicsUserCertificates {
            return EbicsUserCertificates(
                manager.getCertificate("$aliasPrefix-A005"),
                manager.getCertificate("$aliasPrefix-X002"),
                manager.getCertificate("$aliasPrefix-E002"),
                manager.getPrivateKey("$aliasPrefix-A005"),
                manager.getPrivateKey("$aliasPrefix-X002"),
                manager.getPrivateKey("$aliasPrefix-E002")
            )
        }

        @Throws(GeneralSecurityException::class, IOException::class)
        fun create(userDn: String): EbicsUserCertificates {
            try {
                val calendar: Calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION)
                val endDate = Date(calendar.timeInMillis)
                val a005pair = createCertificateKeyPair(EbicsKeyType.A005, userDn, endDate)
                val x002pair = createCertificateKeyPair(EbicsKeyType.X002, userDn, endDate)
                val e002pair = createCertificateKeyPair(EbicsKeyType.E002, userDn, endDate)
                return EbicsUserCertificates(
                    a005pair.certificate,
                    x002pair.certificate,
                    e002pair.certificate,
                    a005pair.privateKey,
                    x002pair.privateKey,
                    e002pair.privateKey,
                )
            } catch (ex: Exception) {
                throw IllegalArgumentException("Cant create certificate for dn='$userDn' error: ${ex.message}", ex)
            }
        }

        @Throws(GeneralSecurityException::class, IOException::class)
        private fun createCertificateKeyPair(
            keyType: EbicsKeyType,
            userDn: String,
            end: Date
        ): CertKeyPair {
            val keypair: KeyPair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE)
            val cert = X509Generator.generateCertificate(keypair, userDn, Date(), end, keyType)
            return CertKeyPair(cert, keypair.private)
        }
    }
}