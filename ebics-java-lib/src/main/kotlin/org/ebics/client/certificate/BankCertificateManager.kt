package org.ebics.client.certificate

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Provider
import java.security.Security
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.logging.Level
import java.util.logging.Logger


open class BankCertificateManager(
    val e002Digest: ByteArray,
    val x002Digest: ByteArray,
    val e002Key: RSAPublicKey,
    val x002Key: RSAPublicKey,
    e002Certificate:X509Certificate? = null,
    x002Certificate:X509Certificate? = null,
    val e002CertificateDigest: ByteArray? = null,
    val x002CertificateDigest: ByteArray? = null,
)  {
    val useCertificates: Boolean = e002Certificate != null && x002Certificate != null

    companion object {
        @JvmStatic
        fun createFromCertificates(bankE002Certificate:ByteArray, bankX002Certificate: ByteArray): BankCertificateManager {
            val e002Certificate: X509Certificate = readCertificate(ByteArrayInputStream(bankE002Certificate))
            val x002Certificate: X509Certificate = readCertificate(ByteArrayInputStream(bankX002Certificate))
            val sha256digest = MessageDigest.getInstance("SHA-256", BouncyCastleProvider.PROVIDER_NAME);
            val e002CertificateDigest = sha256digest.digest(bankE002Certificate)
            val x002CertificateDigest = sha256digest.digest(bankX002Certificate)
            val e002Key: RSAPublicKey = e002Certificate.publicKey as RSAPublicKey
            val x002Key: RSAPublicKey = x002Certificate.publicKey as RSAPublicKey
            return BankCertificateManager(
                KeyUtil.getKeyHash(e002Key),
                KeyUtil.getKeyHash(x002Key),
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
            val e002Key: RSAPublicKey = getPublicKey(BigInteger(bankE002PublicKeyExponent), BigInteger(bankE002PublicKeyModulus))
            val x002Key: RSAPublicKey = getPublicKey(BigInteger(bankX002PublicKeyExponent), BigInteger(bankX002PublicKeyModulus))
            return BankCertificateManager(
                KeyUtil.getKeyHash(e002Key),
                KeyUtil.getKeyHash(x002Key),
                e002Key, x002Key,
            )
        }

        /**
         * Reads a certificate from an input stream for a given provider
         * @param input the input stream
         * @return the certificate
         * @throws CertificateException
         * @throws IOException
         */
        @Throws(CertificateException::class, IOException::class)
        private fun readCertificate(input: InputStream): X509Certificate = readCertificate(input, Security.getProvider(
            BouncyCastleProvider.PROVIDER_NAME))


        /**
         * Reads a certificate from an input stream for a given provider
         * @param input the input stream
         * @param provider the certificate provider
         * @return the certificate
         * @throws CertificateException
         * @throws IOException
         */
        @Throws(CertificateException::class, IOException::class)
        private fun readCertificate(input: InputStream, provider: Provider): X509Certificate {
            val certificate = CertificateFactory.getInstance("X.509", provider).generateCertificate(input)
            return if (certificate == null) {
                PEMParser(InputStreamReader(input)).readObject() as X509Certificate
            } else {
                certificate as X509Certificate
            }
        }

        private fun getPublicKey(publicExponent: BigInteger, modulus: BigInteger): RSAPublicKey {
            return try {
                KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(modulus, publicExponent)) as RSAPublicKey
            } catch (ex: Exception) {
                Logger.getLogger(KeyStoreManager::class.java.name).log(Level.SEVERE, "Exception creating bank public key from exponent & modulus", ex)
                throw ex
            }
        }

        /**
         * Reconstruct the RSAPublicKey from its encoded version (RSAPublicKey.getEncoded())
         */
        private fun getPublicKey(pubKeyEncoded: ByteArray): RSAPublicKey {
            return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(pubKeyEncoded)) as RSAPublicKey
        }
    }
}