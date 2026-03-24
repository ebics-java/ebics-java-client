package org.kopi.ebics.certificate;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.apache.xml.security.Init;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsPartner;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.interfaces.PasswordCallback;

class SignatureVersionTest {

    @BeforeAll
    static void setup() {
        Init.init();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void lookupRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> SignatureVersion.lookup(null));
    }

    @Test
    void lookupRejectsInvalidVersion() {
        assertThrows(IllegalArgumentException.class, () -> SignatureVersion.lookup("a006"));
        assertThrows(IllegalArgumentException.class, () -> SignatureVersion.lookup("A007"));
        assertThrows(IllegalArgumentException.class, () -> SignatureVersion.lookup(""));
    }

    @Test
    void lookupReturnsCorrectInstances() {
        assertSame(SignatureVersion.A005, SignatureVersion.lookup("A005"));
        assertSame(SignatureVersion.A006, SignatureVersion.lookup("A006"));
    }

    @Test
    void a005SignatureAlgorithm() {
        assertEquals("SHA256WithRSA", SignatureVersion.A005.getSignatureAlgorithm());
    }

    @Test
    void a006SignatureAlgorithm() {
        assertEquals("SHA256withRSA/PSS", SignatureVersion.A006.getSignatureAlgorithm());
    }

    @Test
    void a005CertificateAlgorithm() {
        assertEquals("SHA256WithRSAEncryption", SignatureVersion.A005.getCertificateSignatureAlgorithm());
    }

    @Test
    void a006CertificateAlgorithm() {
        assertEquals("SHA256WithRSAAndMGF1", SignatureVersion.A006.getCertificateSignatureAlgorithm());
    }

    @Test
    void a005VersionString() {
        assertEquals("A005", SignatureVersion.A005.getVersion());
    }

    @Test
    void a006VersionString() {
        assertEquals("A006", SignatureVersion.A006.getVersion());
    }

    @Test
    void a005SignAndVerify() throws Exception {
        KeyPair keyPair = KeyUtil.makeKeyPair(2048);
        byte[] data = "EBICS test message for A005 signature".getBytes();

        Signature signer = SignatureVersion.A005.createSignature(keyPair.getPrivate());
        signer.update(data);
        byte[] sig = signer.sign();

        assertNotNull(sig);
        assertTrue(sig.length > 0);

        Signature verifier = SignatureVersion.A005.createVerifySignature(keyPair.getPublic());
        verifier.update(data);
        assertTrue(verifier.verify(sig), "A005 signature verification must succeed");
    }

    @Test
    void a006SignAndVerify() throws Exception {
        KeyPair keyPair = KeyUtil.makeKeyPair(2048);
        byte[] data = "EBICS test message for A006 signature".getBytes();

        Signature signer = SignatureVersion.A006.createSignature(keyPair.getPrivate());
        signer.update(data);
        byte[] sig = signer.sign();

        assertNotNull(sig);
        assertTrue(sig.length > 0);

        Signature verifier = SignatureVersion.A006.createVerifySignature(keyPair.getPublic());
        verifier.update(data);
        assertTrue(verifier.verify(sig), "A006 signature verification must succeed");
    }

    @Test
    void a005AndA006SignaturesAreNotInterchangeable() throws Exception {
        KeyPair keyPair = KeyUtil.makeKeyPair(2048);
        byte[] data = "EBICS cross-version test".getBytes();

        // Sign with A005
        Signature a005Signer = SignatureVersion.A005.createSignature(keyPair.getPrivate());
        a005Signer.update(data);
        byte[] a005Sig = a005Signer.sign();

        // Verify with A006 should fail
        Signature a006Verifier = SignatureVersion.A006.createVerifySignature(keyPair.getPublic());
        a006Verifier.update(data);
        assertFalse(a006Verifier.verify(a005Sig),
            "A005 signature must not verify as A006");

        // Sign with A006
        Signature a006Signer = SignatureVersion.A006.createSignature(keyPair.getPrivate());
        a006Signer.update(data);
        byte[] a006Sig = a006Signer.sign();

        // Verify with A005 should fail
        Signature a005Verifier = SignatureVersion.A005.createVerifySignature(keyPair.getPublic());
        a005Verifier.update(data);
        assertFalse(a005Verifier.verify(a006Sig),
            "A006 signature must not verify as A005");
    }

    @Test
    void a005CertificateGeneration() throws Exception {
        var generator = new X509Generator();
        KeyPair keyPair = KeyUtil.makeKeyPair(2048);
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 365);

        X509Certificate cert = generator.generateA005Certificate(keyPair, "CN=test-a005", now, cal.getTime());

        assertNotNull(cert);
        assertEquals("SHA256WITHRSA", cert.getSigAlgName());
        assertEquals("CN=test-a005", cert.getSubjectX500Principal().getName(X500Principal.RFC2253));
    }

    @Test
    void a006CertificateGeneration() throws Exception {
        var generator = new X509Generator();
        KeyPair keyPair = KeyUtil.makeKeyPair(2048);
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 365);

        X509Certificate cert = generator.generateSignatureCertificate(
            keyPair, "CN=test-a006", now, cal.getTime(), "A006");

        assertNotNull(cert);
        assertTrue(cert.getSigAlgName().contains("MGF1") || cert.getSigAlgName().contains("PSS"),
            "A006 certificate must use PSS/MGF1 algorithm, got: " + cert.getSigAlgName());
        assertEquals("CN=test-a006", cert.getSubjectX500Principal().getName(X500Principal.RFC2253));
        cert.checkValidity(new Date());
        cert.verify(keyPair.getPublic());
    }

    @Test
    void a006CertificateManagerCreateAndLoad() throws Exception {
        var user = createStubUser();
        var manager = new CertificateManager(user);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION);

        manager.createSignatureCertificate(new Date(calendar.getTimeInMillis()), "A006");

        var cert = manager.getSignatureCertificate();
        assertNotNull(cert);
        assertTrue(cert.getSigAlgName().contains("MGF1") || cert.getSigAlgName().contains("PSS"),
            "A006 certificate must use PSS/MGF1 algorithm, got: " + cert.getSigAlgName());
        assertEquals(3, cert.getVersion());
    }

    @Test
    void a006SignatureIsDeterministicInLength() throws Exception {
        KeyPair keyPair = KeyUtil.makeKeyPair(2048);
        for (int i = 0; i < 5; i++) {
            byte[] data = ("message " + i).getBytes();
            Signature signer = SignatureVersion.A006.createSignature(keyPair.getPrivate());
            signer.update(data);
            byte[] sig = signer.sign();
            assertEquals(256, sig.length,
                "A006 signature length should match RSA key size (2048 bits = 256 bytes)");
        }
    }

    @Test
    void a005SignatureLength() throws Exception {
        KeyPair keyPair = KeyUtil.makeKeyPair(2048);
        byte[] data = "test".getBytes();
        Signature signer = SignatureVersion.A005.createSignature(keyPair.getPrivate());
        signer.update(data);
        byte[] sig = signer.sign();
        assertEquals(256, sig.length,
            "A005 signature length should match RSA key size (2048 bits = 256 bytes)");
    }

    @Test
    void pkcs12RoundTripWithA006Certificate() throws Exception {
        var user = createStubUser();
        var manager = new CertificateManager(user);

        manager.create("A006");

        var baos = new ByteArrayOutputStream();
        char[] password = "test".toCharArray();
        manager.writePKCS12Certificate(password, baos);

        var bais = new ByteArrayInputStream(baos.toByteArray());
        var keyStore = KeyStore.getInstance("PKCS12", new BouncyCastleProvider());
        keyStore.load(bais, password);

        var cert = (X509Certificate) keyStore.getCertificate("-A006");
        assertNotNull(cert, "Signature certificate must be loadable from PKCS12 store under -A006 alias");
        assertTrue(cert.getSigAlgName().contains("MGF1") || cert.getSigAlgName().contains("PSS"),
            "Loaded A006 certificate must use PSS/MGF1, got: " + cert.getSigAlgName());

        assertNull(keyStore.getCertificate("-A005"),
            "A006 keystore should not have an -A005 alias");
    }

    @Test
    void pkcs12RoundTripWithA005Certificate() throws Exception {
        var user = createStubUser();
        var manager = new CertificateManager(user);

        manager.create("A005");

        var baos = new ByteArrayOutputStream();
        char[] password = "test".toCharArray();
        manager.writePKCS12Certificate(password, baos);

        var bais = new ByteArrayInputStream(baos.toByteArray());
        var keyStore = KeyStore.getInstance("PKCS12", new BouncyCastleProvider());
        keyStore.load(bais, password);

        var cert = (X509Certificate) keyStore.getCertificate("-A005");
        assertNotNull(cert, "Signature certificate must be loadable from PKCS12 store under -A005 alias");
        assertEquals("SHA256WITHRSA", cert.getSigAlgName());
    }

    @Test
    void lookupSignAndVerifyRoundTrip() throws Exception {
        // Test the full lookup-based flow as used by User.sign()
        KeyPair keyPair = KeyUtil.makeKeyPair(2048);
        byte[] data = "lookup round-trip test".getBytes();

        for (String version : new String[]{"A005", "A006"}) {
            SignatureVersion sv = SignatureVersion.lookup(version);
            Signature signer = sv.createSignature(keyPair.getPrivate());
            signer.update(data);
            byte[] sig = signer.sign();

            Signature verifier = sv.createVerifySignature(keyPair.getPublic());
            verifier.update(data);
            assertTrue(verifier.verify(sig), version + " lookup round-trip failed");
        }
    }

    private EbicsUser createStubUser() {
        return new EbicsUser() {
            @Override public RSAPublicKey getA005PublicKey() { return null; }
            @Override public RSAPublicKey getE002PublicKey() { return null; }
            @Override public RSAPublicKey getX002PublicKey() { return null; }
            @Override public byte[] getA005Certificate() throws EbicsException { return new byte[0]; }
            @Override public byte[] getX002Certificate() throws EbicsException { return new byte[0]; }
            @Override public byte[] getE002Certificate() throws EbicsException { return new byte[0]; }
            @Override public void setA005Certificate(X509Certificate c) {}
            @Override public void setX002Certificate(X509Certificate c) {}
            @Override public void setE002Certificate(X509Certificate c) {}
            @Override public void setA005PrivateKey(PrivateKey k) {}
            @Override public void setX002PrivateKey(PrivateKey k) {}
            @Override public void setE002PrivateKey(PrivateKey k) {}
            @Override public String getSecurityMedium() { return ""; }
            @Override public EbicsPartner getPartner() { return null; }
            @Override public String getUserId() { return ""; }
            @Override public String getName() { return "test-name"; }
            @Override public String getDN() { return "CN=test-dn"; }
            @Override public PasswordCallback getPasswordCallback() { return null; }
            @Override public byte[] authenticate(byte[] digest) throws GeneralSecurityException { return new byte[0]; }
            @Override public byte[] sign(byte[] digest) throws IOException, GeneralSecurityException { return new byte[0]; }
            @Override public byte[] decrypt(byte[] encryptedKey, byte[] transactionKey) { return new byte[0]; }
        };
    }
}
