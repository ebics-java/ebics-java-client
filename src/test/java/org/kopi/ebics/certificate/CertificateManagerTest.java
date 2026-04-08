package org.kopi.ebics.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.apache.xml.security.Init;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsPartner;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.interfaces.PasswordCallback;

class CertificateManagerTest {
    static {
        Init.init();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void createA005Certificate() throws GeneralSecurityException, IOException {
        var user = testUser();
        var manager = new CertificateManager(user);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION);

        manager.createA005Certificate(new Date(calendar.getTimeInMillis()));

        var cert = manager.getA005Certificate();

        assertNotNull(cert);

        //System.out.println(cert);

        assertEquals(3, cert.getVersion(), "Certificate version must be 3 (V3).");
        String expectedDN = "CN=test-dn";
        assertEquals(expectedDN, cert.getIssuerX500Principal().getName(X500Principal.RFC2253));
        assertEquals(expectedDN, cert.getSubjectX500Principal().getName(X500Principal.RFC2253));
        assertEquals("SHA256WITHRSA", cert.getSigAlgName());
    }

    @Test
    void createUsesConfiguredKeyLength() throws Exception {
        String previousKeyLength = System.getProperty("ebics.key.length");
        System.setProperty("ebics.key.length", "3072");
        try {
            var manager = new CertificateManager(testUser());
            manager.create();
            var cert = manager.getA005Certificate();
            assertNotNull(cert);
            assertEquals(3072, ((RSAPublicKey) cert.getPublicKey()).getModulus().bitLength());
        } finally {
            if (previousKeyLength == null) {
                System.clearProperty("ebics.key.length");
            } else {
                System.setProperty("ebics.key.length", previousKeyLength);
            }
        }
    }

    @Test
    void createUsesConfiguredCertificateValidityYears() throws Exception {
        String previousValidityYears = System.getProperty("ebics.cert.validity.years");
        System.setProperty("ebics.cert.validity.years", "2");
        try {
            var manager = new CertificateManager(testUser());
            manager.create();
            var cert = manager.getA005Certificate();
            assertNotNull(cert);
            long validDays = ChronoUnit.DAYS.between(
                cert.getNotBefore().toInstant(),
                cert.getNotAfter().toInstant());
            assertTrue(validDays >= 730 && validDays <= 732);
        } finally {
            if (previousValidityYears == null) {
                System.clearProperty("ebics.cert.validity.years");
            } else {
                System.setProperty("ebics.cert.validity.years", previousValidityYears);
            }
        }
    }

    private EbicsUser testUser() {
        return new EbicsUser() {
            @Override
            public RSAPublicKey getA005PublicKey() {
                return null;
            }

            @Override
            public RSAPublicKey getE002PublicKey() {
                return null;
            }

            @Override
            public RSAPublicKey getX002PublicKey() {
                return null;
            }

            @Override
            public byte[] getA005Certificate() throws EbicsException {
                return new byte[0];
            }

            @Override
            public byte[] getX002Certificate() throws EbicsException {
                return new byte[0];
            }

            @Override
            public byte[] getE002Certificate() throws EbicsException {
                return new byte[0];
            }

            @Override
            public void setA005Certificate(X509Certificate a005certificate) {

            }

            @Override
            public void setX002Certificate(X509Certificate x002certificate) {

            }

            @Override
            public void setE002Certificate(X509Certificate e002certificate) {

            }

            @Override
            public void setA005PrivateKey(PrivateKey a005Key) {

            }

            @Override
            public void setX002PrivateKey(PrivateKey x002Key) {

            }

            @Override
            public void setE002PrivateKey(PrivateKey e002Key) {

            }

            @Override
            public String getSecurityMedium() {
                return "";
            }

            @Override
            public EbicsPartner getPartner() {
                return null;
            }

            @Override
            public String getUserId() {
                return "";
            }

            @Override
            public String getName() {
                return "test-name";
            }

            @Override
            public String getDN() {
                return "CN=test-dn";
            }

            @Override
            public PasswordCallback getPasswordCallback() {
                return null;
            }

            @Override
            public byte[] authenticate(byte[] digest) throws GeneralSecurityException {
                return new byte[0];
            }

            @Override
            public byte[] sign(byte[] digest) throws IOException, GeneralSecurityException {
                return new byte[0];
            }

            @Override
            public byte[] decrypt(byte[] encryptedKey, byte[] transactionKey)
                throws GeneralSecurityException, IOException, EbicsException {
                return new byte[0];
            }
        };
    }
}
