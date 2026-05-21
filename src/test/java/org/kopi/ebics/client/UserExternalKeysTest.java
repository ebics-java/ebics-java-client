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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.apache.xml.security.Init;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kopi.ebics.certificate.X509Generator;
import org.kopi.ebics.interfaces.EbicsPartner;

/**
 * Coverage for the {@link User} constructor that accepts an
 * {@link ExternalKeyProvider} (HSM / smartcard / external key custody).
 *
 * <p>Three guarantees the upstream patch must preserve:
 * <ol>
 *   <li>The externally supplied keys end up on the {@link User} verbatim &mdash;
 *       no in-process key pair is generated and silently substituted.</li>
 *   <li>A null provider or a provider returning {@code null} for any role is
 *       rejected at the boundary with {@link IllegalArgumentException}.</li>
 *   <li>The {@link ExternalKeyProvider.KeyMaterial} record itself validates
 *       its components &mdash; partial records cannot enter the library.</li>
 * </ol>
 */
class UserExternalKeysTest {

    @BeforeAll
    static void initSecurityProviders() {
        Init.init();
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Test
    void constructorAssignsExternalKeysVerbatim() throws Exception {
        RoleKeys a005 = generateRoleKeys("A005");
        RoleKeys x002 = generateRoleKeys("X002");
        RoleKeys e002 = generateRoleKeys("E002");

        ExternalKeyProvider provider = new ExternalKeyProvider() {
            @Override
            public KeyMaterial a005() {
                return new KeyMaterial(a005.privateKey, a005.certificate);
            }

            @Override
            public KeyMaterial x002() {
                return new KeyMaterial(x002.privateKey, x002.certificate);
            }

            @Override
            public KeyMaterial e002() {
                return new KeyMaterial(e002.privateKey, e002.certificate);
            }
        };

        User user = new User(mockPartner(), "USR001", "Test User", "user@example.com",
            "DE", "Test Org", null, provider);

        assertNotNull(user.getA005PublicKey(), "A005 public key must be present");
        assertNotNull(user.getX002PublicKey(), "X002 public key must be present");
        assertNotNull(user.getE002PublicKey(), "E002 public key must be present");

        assertEquals(a005.certificate.getPublicKey(), user.getA005PublicKey(),
            "User must expose the A005 public key from the supplied certificate");
        assertEquals(x002.certificate.getPublicKey(), user.getX002PublicKey(),
            "User must expose the X002 public key from the supplied certificate");
        assertEquals(e002.certificate.getPublicKey(), user.getE002PublicKey(),
            "User must expose the E002 public key from the supplied certificate");

        assertSame(a005.privateKey, privateKeyField(user, "a005PrivateKey"),
            "A005 PrivateKey on User must be the same instance the caller supplied "
                + "(no library-side KeyPair generation)");
        assertSame(x002.privateKey, privateKeyField(user, "x002PrivateKey"),
            "X002 PrivateKey on User must be the same instance the caller supplied");
        assertSame(e002.privateKey, privateKeyField(user, "e002PrivateKey"),
            "E002 PrivateKey on User must be the same instance the caller supplied");
    }

    @Test
    void nullProviderRejected() {
        assertThrows(IllegalArgumentException.class, () -> new User(
            mockPartner(), "USR001", "Test User", "user@example.com",
            "DE", "Test Org", null, (ExternalKeyProvider) null));
    }

    @Test
    void partialProviderRejected() throws Exception {
        RoleKeys a005 = generateRoleKeys("A005");

        // Provider with only A005 populated; X002 returns null.
        ExternalKeyProvider partial = new ExternalKeyProvider() {
            @Override
            public KeyMaterial a005() {
                return new KeyMaterial(a005.privateKey, a005.certificate);
            }

            @Override
            public KeyMaterial x002() {
                return null;
            }

            @Override
            public KeyMaterial e002() {
                return null;
            }
        };

        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> new User(
            mockPartner(), "USR001", "Test User", "user@example.com",
            "DE", "Test Org", null, partial));

        // Message must identify the offending role for ops debuggability.
        assertNotNull(iae.getMessage(), "IllegalArgumentException must carry a message");
        org.junit.jupiter.api.Assertions.assertTrue(
            iae.getMessage().contains("x002"),
            "Boundary error must name the missing role; got: " + iae.getMessage());
    }

    @Test
    void keyMaterialRecordRejectsNullPrivateKey() throws Exception {
        RoleKeys a005 = generateRoleKeys("A005");
        assertThrows(IllegalArgumentException.class,
            () -> new ExternalKeyProvider.KeyMaterial(null, a005.certificate));
    }

    @Test
    void keyMaterialRecordRejectsNullCertificate() throws Exception {
        RoleKeys a005 = generateRoleKeys("A005");
        assertThrows(IllegalArgumentException.class,
            () -> new ExternalKeyProvider.KeyMaterial(a005.privateKey, null));
    }

    // ---------- helpers ----------

    private static RoleKeys generateRoleKeys(String role) throws GeneralSecurityException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();
        X509Certificate cert;
        try {
            X509Generator generator = new X509Generator();
            switch (role) {
                case "A005":
                    cert = generator.generateA005Certificate(pair, "CN=test-" + role, new Date(), end);
                    break;
                case "X002":
                    cert = generator.generateX002Certificate(pair, "CN=test-" + role, new Date(), end);
                    break;
                default:
                    cert = generator.generateE002Certificate(pair, "CN=test-" + role, new Date(), end);
                    break;
            }
        } catch (Exception e) {
            throw new GeneralSecurityException("Test cert generation for role " + role + " failed", e);
        }
        return new RoleKeys(pair.getPrivate(), cert);
    }

    private static EbicsPartner mockPartner() {
        return org.mockito.Mockito.mock(EbicsPartner.class);
    }

    private static PrivateKey privateKeyField(User user, String fieldName) throws Exception {
        java.lang.reflect.Field f = User.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (PrivateKey) f.get(user);
    }

    private record RoleKeys(PrivateKey privateKey, X509Certificate certificate) {
    }
}
