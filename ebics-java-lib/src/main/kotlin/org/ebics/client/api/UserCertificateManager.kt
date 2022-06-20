package org.ebics.client.api

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.exception.EbicsException
import org.ebics.client.utils.CryptoUtils
import org.ebics.client.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


interface UserCertificateManager {
    val a005Certificate: X509Certificate
    val x002Certificate: X509Certificate
    val e002Certificate: X509Certificate
    val a005PrivateKey: PrivateKey
    val x002PrivateKey: PrivateKey
    val e002PrivateKey: PrivateKey

    @Throws(EbicsException::class)
    fun getA005CertificateBytes(): ByteArray {
        return try {
            a005Certificate.encoded
        } catch (e: CertificateEncodingException) {
            throw EbicsException(e.message)
        }
    }

    @Throws(EbicsException::class)
    fun getE002CertificateBytes(): ByteArray {
        return try {
            e002Certificate.encoded
        } catch (e: CertificateEncodingException) {
            throw EbicsException(e.message)
        }
    }

    @Throws(EbicsException::class)
    fun getX002CertificateBytes(): ByteArray {
        return try {
            x002Certificate.encoded
        } catch (e: CertificateEncodingException) {
            throw EbicsException(e.message)
        }
    }

    val a005PublicKey: RSAPublicKey
        get() = a005Certificate.publicKey as RSAPublicKey
    val e002PublicKey: RSAPublicKey
        get() = e002Certificate.publicKey as RSAPublicKey
    val x002PublicKey: RSAPublicKey
        get() = x002Certificate.publicKey as RSAPublicKey

    /**
     * EBICS Specification 2.4.2 - 11.1.1 Process:
     *
     *
     * Identification and authentication signatures are based on the RSA signature process.
     * The following parameters determine the identification and authentication signature process:
     *
     *
     *  1.  Length of the (secret) RSA key
     *  1.  Hash algorithm
     *  1.  Padding process
     *  1.  Canonisation process.
     *
     *
     *
     * For the identification and authentication process, EBICS defines the process “X002” with
     * the following parameters:
     *
     *  1. Key length in Kbit >=1Kbit (1024 bit) and lesser than 16Kbit
     *  1. Hash algorithm SHA-256
     *  1. Padding process: PKCS#1
     *  1. Canonisation process: http://www.w3.org/TR/2001/REC-xml-c14n-20010315
     *
     *
     *
     * From EBICS 2.4 on, the customer system must use the hash value of the public bank key
     * X002 in a request.
     *
     *
     * Notes:
     *
     *  1.  The key length is defined else where.
     *  1.  The padding is performed by the [Signature] class.
     *  1.  The digest is already canonized in the [sign(byte[])][SignedInfo.sign]
     *
     */
    @Throws(GeneralSecurityException::class)
    fun authenticate(digest: ByteArray): ByteArray {
        val signature: Signature = Signature.getInstance("SHA256WithRSA", BouncyCastleProvider.PROVIDER_NAME)
        signature.initSign(x002PrivateKey)
        signature.update(digest)
        return signature.sign()
    }

    /**
     * EBICS Specification 2.4.2 - 14.1 Version A005/A006 of the electronic signature:
     *
     *
     * For the signature processes A005 an interval of 1536 bit (minimum)
     * and 4096 bit (maximum) is defined for the key length.
     *
     *
     * The digital signature mechanisms A005 is both based on the industry standard
     * [PKCS1] using the hash algorithm SHA-256. They are both signature mechanisms without
     * message recovery.
     *
     *
     * A hash algorithm maps bit sequences of arbitrary length (input bit sequences) to byte
     * sequences of a fixed length, determined by the Hash algorithm. The result of the execution of
     * a Hash algorithm to a bit sequence is defined as hash value.
     *
     *
     * The hash algorithm SHA-256 is specified in [FIPS H2]. SHA-256 maps input bit sequences of
     * arbitrary length to byte sequences of 32 byte length. The padding of input bit sequences to a
     * length being a multiple of 64 byte is part of the hash algorithm. The padding even is applied if
     * the input bit sequence already has a length that is a multiple of 64 byte.
     *
     *
     * SHA-256 processes the input bit sequences in blocks of 64 byte length.
     * The hash value of a bit sequence x under the hash algorithm SHA-256 is referred to as
     * follows: SHA-256(x).
     *
     *
     * The digital signature mechanism A005 is identical to EMSA-PKCS1-v1_5 using the hash
     * algorithm SHA-256. The byte length H of the hash value is 32.
     *
     * According [PKCS1] (using the method EMSA-PKCS1-v1_5) the following steps shall be
     * performed for the computation of a signature for message M with bit length m.
     *
     *  1.  The hash value HASH(M) of the byte length H shall be computed. In the case of A005
     * SHA-256(M) with a length of 32 bytes.
     *  1.  The DSI for the signature algorithm shall be generated.
     *  1.  A signature shall be computed using the DSI with the standard algorithm for the
     * signature generation described in section 14.1.3.1 of the EBICS specification (V 2.4.2).
     *
     *
     *
     * The [Signature] is a digital signature scheme with
     * appendix (SSA) combining the RSA algorithm with the EMSA-PKCS1-v1_5 encoding
     * method.
     *
     *
     *  The `digest` will be signed with the RSA user signature key using the
     * [Signature] that will be instantiated with the **SHA-256**
     * algorithm. This signature is then put in a [UserSignature] XML object that
     * will be sent to the EBICS server.
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    fun sign(digest: ByteArray): ByteArray {
        val signature = Signature.getInstance("SHA256WithRSA", BouncyCastleProvider.PROVIDER_NAME)
        signature.initSign(a005PrivateKey)
        signature.update(removeOSSpecificChars(digest))
        return signature.sign()
    }

    /**
     * EBICS IG CFONB VF 2.1.4 2012 02 24 - 2.1.3.2 Calcul de la signature:
     *
     *
     * Il convient d’utiliser PKCS1 V1.5 pour chiffrer la clé de chiffrement.
     *
     *
     * EBICS Specification 2.4.2 - 15.2 Workflows at the recipient’s end:
     *
     *
     * **Decryption of the DES key**
     *
     * The leading 256 null bits of the EDEK are removed and the remaining 768 bits are decrypted
     * with the recipient’s secret key of the RSA key system. PDEK is then present. The secret DES
     * key DEK is obtained from the lowest-value 128 bits of PDEK, this is split into the individual
     * keys DEK<SUB>left</SUB> and DEK<SUB>right</SUB>.
     */
    @Throws(EbicsException::class, GeneralSecurityException::class, IOException::class)
    fun decrypt(encryptedData: ByteArray, transactionKey: ByteArray): ByteArray {
        val cipher: Cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME)
        cipher.init(Cipher.DECRYPT_MODE, e002PrivateKey)
        val blockSize: Int = cipher.blockSize
        val outputStream = ByteArrayOutputStream()
        var j = 0
        while (j * blockSize < transactionKey.size) {
            outputStream.write(cipher.doFinal(transactionKey, j * blockSize, blockSize))
            j++
        }
        return decryptData(encryptedData, outputStream.toByteArray())
    }

    /**
     * Decrypts the `encryptedData` using the decoded transaction key.
     *
     *
     * EBICS Specification 2.4.2 - 15.2 Workflows at the recipient’s end:
     *
     *
     * **Decryption of the message**
     *
     * The encrypted original message is decrypted in CBC mode in accordance with the 2-key
     * triple DES process via the secret DES key (comprising DEK<SUB>left</SUB> and DEK<SUP>right<SUB>).
     * In doing this, the following initialization value ICV is again used.
     *
    </SUB></SUP> *
     * **Removal of the padding information**
     *
     * The method “Padding with Octets” according to ANSI X9.23 is used to remove the padding
     * information from the decrypted message. The original message is then available in decrypted
     * form.
     *
     * @param input The encrypted data
     * @param key The secret key.
     * @return The decrypted data sent from the EBICS bank.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(EbicsException::class)
    private fun decryptData(input: ByteArray, key: ByteArray): ByteArray {
        return CryptoUtils.decrypt(input, SecretKeySpec(key, "EAS"))
    }

    /**
     * EBICS Specification 2.4.2 - 7.1 Process description:
     *
     *
     * In particular, so-called “white-space characters” such as spaces, tabs, carriage
     * returns and line feeds (“CR/LF”) are not permitted.
     *
     *
     *  All white-space characters should be removed from entry buffer `buf`.
     *
     * @param buf the given byte buffer
     * @return The byte buffer portion corresponding to the given length and offset
     */
    fun removeOSSpecificChars(buf: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        for (i in buf.indices) {
            when (buf[i]) {
                '\r'.toByte(), '\n'.toByte(), 0x1A.toByte() -> {
                }
                else -> output.write(buf[i].toInt())
            }
        }
        return output.toByteArray()
    }
}