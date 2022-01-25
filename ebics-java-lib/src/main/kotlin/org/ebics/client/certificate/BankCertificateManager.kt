package org.ebics.client.certificate

import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey


open class BankCertificateManager(
    val e002Digest: ByteArray,
    val x002Digest: ByteArray,
    val e002Key: RSAPublicKey,
    val x002Key: RSAPublicKey,
    private val e002Certificate:X509Certificate? = null,
    private val x002Certificate:X509Certificate? = null,
    val e002CertificateDigest: ByteArray? = null,
    val x002CertificateDigest: ByteArray? = null,
)  {
    val useCertificates: Boolean = e002Certificate != null && x002Certificate != null

    companion object {
        @JvmStatic
        fun createFromCertificates(bankE002Certificate:ByteArray, bankX002Certificate: ByteArray): BankCertificateManager {
            val e002Certificate: X509Certificate = KeyStoreManager.readCertificate(ByteArrayInputStream(bankE002Certificate))
            val x002Certificate: X509Certificate = KeyStoreManager.readCertificate(ByteArrayInputStream(bankX002Certificate))
            val sha256digest = MessageDigest.getInstance("SHA-256", "BC");
            val e002CertificateDigest = sha256digest.digest(bankE002Certificate)
            val x002CertificateDigest = sha256digest.digest(bankX002Certificate)
            val e002Key: RSAPublicKey = e002Certificate.publicKey as RSAPublicKey
            val x002Key: RSAPublicKey = x002Certificate.publicKey as RSAPublicKey
            return BankCertificateManager(
                KeyUtil.getKeyDigest(e002Key),
                KeyUtil.getKeyDigest(x002Key),
                e002Key, x002Key,
                e002Certificate,
                x002Certificate,
                e002CertificateDigest,
                x002CertificateDigest,
            )
        }

        @JvmStatic
        fun createFromPubKeyExponentAndModulus(bankE002PublicKeyExponent:ByteArray, bankE002PublicKeyModulus:ByteArray,
                                               bankX002PublicKeyExponent:ByteArray, bankX002PublicKeyModulus:ByteArray):BankCertificateManager {
            val e002Key: RSAPublicKey = KeyStoreManager.getPublicKey(BigInteger(bankE002PublicKeyExponent), BigInteger(bankE002PublicKeyModulus))
            val x002Key: RSAPublicKey = KeyStoreManager.getPublicKey(BigInteger(bankX002PublicKeyExponent), BigInteger(bankX002PublicKeyModulus))
            return BankCertificateManager(
                KeyUtil.getKeyDigest(e002Key),
                KeyUtil.getKeyDigest(x002Key),
                e002Key, x002Key,
            )
        }
    }
}