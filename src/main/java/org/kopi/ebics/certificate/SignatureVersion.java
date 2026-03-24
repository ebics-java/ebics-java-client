/*
 * Copyright (c) 2026 Uwe Maurer
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
 */

package org.kopi.ebics.certificate;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Encapsulates signature algorithm details for EBICS signature versions.
 *
 * <p>Two implementations are provided as constants:
 * <ul>
 *   <li>{@link #A005} — EMSA-PKCS1-v1_5 with SHA-256
 *   <li>{@link #A006} — EMSA-PSS with SHA-256, MGF1(SHA-256), salt length 32 bytes
 * </ul>
 */
public interface SignatureVersion {

    /**
     * EBICS signature version A005: EMSA-PKCS1-v1_5 with SHA-256.
     */
    SignatureVersion A005 = new SignatureVersion() {
        @Override
        public String getVersion() {
            return "A005";
        }

        @Override
        public String getSignatureAlgorithm() {
            return "SHA256WithRSA";
        }

        @Override
        public String getCertificateSignatureAlgorithm() {
            return "SHA256WithRSAEncryption";
        }

        @Override
        public Signature createSignature(PrivateKey privateKey) throws GeneralSecurityException {
            Signature signature = Signature.getInstance(getSignatureAlgorithm(), BouncyCastleProvider.PROVIDER_NAME);
            signature.initSign(privateKey);
            return signature;
        }

        @Override
        public Signature createVerifySignature(PublicKey publicKey) throws GeneralSecurityException {
            Signature signature = Signature.getInstance(getSignatureAlgorithm(), BouncyCastleProvider.PROVIDER_NAME);
            signature.initVerify(publicKey);
            return signature;
        }
    };

    /**
     * EBICS signature version A006: EMSA-PSS with SHA-256, MGF1(SHA-256), salt length 32 bytes.
     */
    SignatureVersion A006 = new SignatureVersion() {

        private final PSSParameterSpec pssParams = new PSSParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1
        );

        @Override
        public String getVersion() {
            return "A006";
        }

        @Override
        public String getSignatureAlgorithm() {
            return "SHA256withRSA/PSS";
        }

        @Override
        public String getCertificateSignatureAlgorithm() {
            return "SHA256WithRSAAndMGF1";
        }

        @Override
        public Signature createSignature(PrivateKey privateKey) throws GeneralSecurityException {
            Signature signature = Signature.getInstance(getSignatureAlgorithm(), BouncyCastleProvider.PROVIDER_NAME);
            signature.setParameter(pssParams);
            signature.initSign(privateKey);
            return signature;
        }

        @Override
        public Signature createVerifySignature(PublicKey publicKey) throws GeneralSecurityException {
            Signature signature = Signature.getInstance(getSignatureAlgorithm(), BouncyCastleProvider.PROVIDER_NAME);
            signature.setParameter(pssParams);
            signature.initVerify(publicKey);
            return signature;
        }
    };

    /**
     * Returns the EBICS version string (e.g. "A005" or "A006").
     */
    String getVersion();

    /**
     * Returns the JCA signature algorithm name used for signing data.
     */
    String getSignatureAlgorithm();

    /**
     * Returns the BouncyCastle algorithm name used for X.509 certificate generation.
     */
    String getCertificateSignatureAlgorithm();

    /**
     * Creates and initializes a {@link Signature} instance for signing.
     */
    Signature createSignature(PrivateKey privateKey) throws GeneralSecurityException;

    /**
     * Creates and initializes a {@link Signature} instance for verification.
     */
    Signature createVerifySignature(PublicKey publicKey) throws GeneralSecurityException;

    /**
     * Looks up a {@link SignatureVersion} by its EBICS version string.
     *
     * @param version the version string (e.g. "A005" or "A006")
     * @return the corresponding SignatureVersion
     * @throws IllegalArgumentException if the version is not supported
     */
    static SignatureVersion lookup(String version) {
        if ("A005".equals(version)) return A005;
        if ("A006".equals(version)) return A006;
        throw new IllegalArgumentException(
            "Unsupported signature version: " + version + ". Must be A005 or A006.");
    }
}
