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
 */

package org.kopi.ebics.client;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Supplies the A005/X002/E002 key material for a subscriber when the caller
 * holds the private keys outside of the JVM heap (HSM, smartcard, or any other
 * PKCS#11 token).
 *
 * <p>Pass an implementation of this interface to
 * {@link EbicsClient#createUser(java.net.URL, String, String, String, String,
 * String, String, String, String, boolean, boolean,
 * org.kopi.ebics.interfaces.PasswordCallback, ExternalKeyProvider)} or to the
 * matching {@link User} constructor; the library will then skip its built-in
 * {@code KeyUtil.makeKeyPair(...)} path and install the supplied keys and
 * certificates on the {@link User}. This is the seam that allows callers to
 * keep DSGVO / FIPS / ISO 27001 commitments under which an EBICS subscriber's
 * private signing key never resides outside its hardware token.
 *
 * <p>Implementations MUST NOT return a {@link PrivateKey} whose
 * {@link PrivateKey#getEncoded()} is non-null when the key is intended to be
 * HSM-resident &mdash; a non-null encoding indicates the key escaped the token.
 * The library does not assert this invariant; callers must.
 *
 * <p>All three role methods must return a non-null {@link KeyMaterial}. Returning
 * {@code null} from any role causes the consuming overload to throw
 * {@link IllegalArgumentException} at the boundary.
 *
 * @since 2.1.0
 */
public interface ExternalKeyProvider {

    /** Bank-technical / electronic-signature key (EBICS role A005). */
    KeyMaterial a005();

    /** Identification &amp; authentication key (EBICS role X002). */
    KeyMaterial x002();

    /** Encryption key (EBICS role E002). */
    KeyMaterial e002();

    /**
     * A {@code (PrivateKey, X509Certificate)} pair for one EBICS role.
     * Both components are required; the compact constructor rejects {@code null}.
     */
    record KeyMaterial(PrivateKey privateKey, X509Certificate certificate) {
        public KeyMaterial {
            if (privateKey == null) {
                throw new IllegalArgumentException("privateKey must not be null");
            }
            if (certificate == null) {
                throw new IllegalArgumentException("certificate must not be null");
            }
        }
    }
}
