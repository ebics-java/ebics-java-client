/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.ebics.client.certificate

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.X509ExtensionUtils
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.OperatorCreationException
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.IOException
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*


/**
 * An X509 certificate generator for EBICS protocol.
 * Generated certificates are self signed certificates.
 *
 * @author hachani
 */
object X509Generator {
    /**
     * Generates the signature certificate for the EBICS protocol
     * @param keypair the key pair
     * @param issuer the certificate issuer
     * @param notBefore the begin validity date
     * @param notAfter the end validity date
     * @return the signature certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun generateA005Certificate(
        keypair: KeyPair,
        issuer: String,
        notBefore: Date,
        notAfter: Date
    ): X509Certificate {
        return generateCertificate(
            keypair,
            issuer,
            notBefore,
            notAfter,
            EbicsKeyType.A005
        )
    }

    /**
     * Generates the authentication certificate for the EBICS protocol
     * @param keypair the key pair
     * @param issuer the certificate issuer
     * @param notBefore the begin validity date
     * @param notAfter the end validity date
     * @return the authentication certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun generateX002Certificate(
        keypair: KeyPair,
        issuer: String,
        notBefore: Date,
        notAfter: Date
    ): X509Certificate {
        return generateCertificate(
            keypair,
            issuer,
            notBefore,
            notAfter,
            EbicsKeyType.X002
        )
    }

    /**
     * Generates the encryption certificate for the EBICS protocol
     * @param keypair the key pair
     * @param issuer the certificate issuer
     * @param notBefore the begin validity date
     * @param notAfter the end validity date
     * @return the encryption certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun generateE002Certificate(
        keypair: KeyPair,
        issuer: String,
        notBefore: Date,
        notAfter: Date
    ): X509Certificate {
        return generateCertificate(
            keypair,
            issuer,
            notBefore,
            notAfter,
            EbicsKeyType.E002
        )
    }

    /**
     * Returns an `X509Certificate` from a given
     * `KeyPair` and limit dates validations
     * @param keypair the given key pair
     * @param issuer the certificate issuer
     * @param notBefore the begin validity date
     * @param notAfter the end validity date
     * @param keyType the EBICS certificate type usage
     * @return the X509 certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun generateCertificate(
        keypair: KeyPair,
        issuer: String,
        notBefore: Date,
        notAfter: Date,
        keyType: EbicsKeyType
    ): X509Certificate {

        val serial: BigInteger = BigInteger.valueOf(generateSerial())
        val subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keypair.public.encoded)
        val builder =
            X509v3CertificateBuilder(X500Name(issuer), serial, notBefore, notAfter, X500Name(issuer), subPubKeyInfo)

        builder.addExtension(
            Extension.basicConstraints,
            false,
            BasicConstraints(true)
        )
        builder.addExtension(
            Extension.subjectKeyIdentifier,
            false,
            getSubjectKeyIdentifier(subPubKeyInfo)
        )
        builder.addExtension(
            Extension.authorityKeyIdentifier,
            false,
            getAuthorityKeyIdentifier(
                subPubKeyInfo,
                issuer,
                serial
            )
        )
        val vector = ASN1EncodableVector()
        vector.add(KeyPurposeId.id_kp_emailProtection)
        builder.addExtension(
            Extension.extendedKeyUsage,
            false,
            ExtendedKeyUsage.getInstance(DERSequence(vector))
        )
        when (keyType) {
            EbicsKeyType.A005 -> builder.addExtension(
                Extension.keyUsage, false, KeyUsage(
                    KeyUsage.nonRepudiation
                )
            )
            EbicsKeyType.X002 -> builder.addExtension(
                Extension.keyUsage, false, KeyUsage(
                    KeyUsage.digitalSignature
                )
            )
            EbicsKeyType.E002 -> builder.addExtension(
                Extension.keyUsage, false, KeyUsage(
                    KeyUsage.keyAgreement
                )
            )
        }
        val signer: ContentSigner =
            JcaContentSignerBuilder(X509Constants.SIGNATURE_ALGORITHM).setProvider(BouncyCastleProvider())
                .build(keypair.private)
        val holder = builder.build(signer)
        val certificate: X509Certificate =
            JcaX509CertificateConverter().setProvider(BouncyCastleProvider()).getCertificate(holder)
        certificate.checkValidity(Date())
        certificate.verify(keypair.public)
        return certificate
    }

    /**
     * Returns the `AuthorityKeyIdentifier` corresponding to a given `PublicKey`
     * @param pubKeyInfo the given public key info
     * @param issuer the certificate issuer
     * @param serial the certificate serial number
     * @return the authority key identifier of the public key
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getAuthorityKeyIdentifier(
        pubKeyInfo: SubjectPublicKeyInfo,
        issuer: String,
        serial: BigInteger
    ): AuthorityKeyIdentifier {
        val digCalc = BcDigestCalculatorProvider()[AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1)]
        val utils = X509ExtensionUtils(digCalc)
        return utils.createAuthorityKeyIdentifier(pubKeyInfo, GeneralNames(GeneralName(X500Name(issuer))), serial)
    }

    /**
     * Returns the `SubjectKeyIdentifier` corresponding to a given `PublicKey`
     * @param pubKeyInfo the given public key info
     * @return the subject key identifier
     * @throws GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class)
    private fun getSubjectKeyIdentifier(pubKeyInfo: SubjectPublicKeyInfo): SubjectKeyIdentifier {
        return try {
            val digCalc = BcDigestCalculatorProvider()[AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1)]
            val utils = X509ExtensionUtils(digCalc)
            utils.createSubjectKeyIdentifier(pubKeyInfo)
        } catch (e: OperatorCreationException) {
            throw GeneralSecurityException(e)
        }
    }

    /**
     * Generates a random serial number
     *
     * @return the serial number
     */
    private fun generateSerial(): Long {
        val now = Date()
        val sNow = sdfSerial.format(now)
        return java.lang.Long.valueOf(sNow).toLong()
    }

    private val sdfSerial: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}