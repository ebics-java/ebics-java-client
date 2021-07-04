package org.ebics.client.certificate

import org.ebics.client.interfaces.PasswordCallback
import sun.security.krb5.Confounder.bytes
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec


open class BankCertificateManager(
    val e002Digest: ByteArray,
    val x002Digest: ByteArray,
    val e002Key: RSAPublicKey,
    val x002Key: RSAPublicKey,
    private val e002Certificate:X509Certificate? = null,
    private val x002Certificate:X509Certificate? = null,
)  {
    val useCertificates: Boolean = e002Certificate != null && x002Certificate != null

    companion object {
        @JvmStatic
        fun createFromCertificates(bankE002Certificate:ByteArray, bankX002Certificate: ByteArray): BankCertificateManager {
            val e002Certificate: X509Certificate = KeyStoreManager.readCertificate(ByteArrayInputStream(bankE002Certificate))
            val x002Certificate: X509Certificate = KeyStoreManager.readCertificate(ByteArrayInputStream(bankX002Certificate))
            val e002Key: RSAPublicKey = e002Certificate.publicKey as RSAPublicKey
            val x002Key: RSAPublicKey = x002Certificate.publicKey as RSAPublicKey
            return BankCertificateManager(
                KeyUtil.getKeyDigest(e002Key),
                KeyUtil.getKeyDigest(x002Key),
                e002Key, x002Key,
                e002Certificate,
                x002Certificate,
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

        @JvmStatic
        fun load(ins: InputStream): BankCertificateManager {
            TODO("Loading from stream to be implemented")
            //val publicKey: PublicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(bytes))
        }
    }

    fun saveCertificates(os:OutputStream, bankId:String, passwordCallback: PasswordCallback) {
        require (e002Certificate != null && x002Certificate != null) {"No certificates found in order to be saved (Create instance with certificates)"}
        val manager = KeyStoreManager.create(passwordCallback.password)
        manager.setCertificateEntry("$bankId-E002", e002Certificate)
        manager.setCertificateEntry("$bankId-X002", x002Certificate)
        manager.save(os)
    }

    fun save(os: ObjectOutputStream) {
        os.write(e002Digest)
        os.write(x002Digest)
        os.write(e002Key.encoded)
        os.write(x002Key.encoded)
        TODO("Store lengths of digest, keys: e002Key, x002Key")
    }
}