/*
 * Copyright Uwe Maurer
 */

package org.kopi.ebics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Locale;

import org.apache.xml.security.Init;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kopi.ebics.certificate.KeyUtil;
import org.kopi.ebics.client.Bank;
import org.kopi.ebics.client.Partner;
import org.kopi.ebics.client.User;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsOrderType;
import org.kopi.ebics.interfaces.InitLetter;
import org.kopi.ebics.letter.A005Letter;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.xml.InitializationRequestElement;

/**
 * Characterization tests that pin down the byte-exact Base64/Hex encoding
 * behavior expected of the letter and request-element code paths. Written
 * during the migration from commons-codec to {@link java.util.Base64} /
 * {@link java.util.HexFormat} so any future change in those code paths
 * stays byte-equivalent.
 */
class CodecMigrationTest {

    @BeforeAll
    static void registerBc() {
        Init.init();
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Test
    void keyUtilDigestIsAsciiUppercaseHexSha256() throws Exception {
        RSAPublicKey key = testKey();

        byte[] digest = KeyUtil.getKeyDigest(key);

        assertEquals(64, digest.length, "SHA-256 hex must be 64 chars");
        for (byte b : digest) {
            boolean isDigit = b >= '0' && b <= '9';
            boolean isUpperHex = b >= 'A' && b <= 'F';
            assertTrue(isDigit || isUpperHex,
                "digest must be UPPERCASE ASCII hex; got byte " + b);
        }
    }

    @Test
    void keyUtilDigestIsDeterministic() throws Exception {
        RSAPublicKey key = testKey();
        assertEquals(
            new String(KeyUtil.getKeyDigest(key), StandardCharsets.US_ASCII),
            new String(KeyUtil.getKeyDigest(key), StandardCharsets.US_ASCII));
    }

    @Test
    void letterCertificateBlockIsChunkedBase64() throws Exception {
        String letter = renderA005Letter(testUser());

        int begin = letter.indexOf("-----BEGIN CERTIFICATE-----");
        int end = letter.indexOf("-----END CERTIFICATE-----");
        assertTrue(begin >= 0 && end > begin, "letter must contain a certificate block");

        String body = letter.substring(begin + "-----BEGIN CERTIFICATE-----".length(), end).trim();
        String[] lines = body.split("\\r?\\n");
        assertTrue(lines.length >= 2, "chunked Base64 produces multiple lines, got " + lines.length);
        for (String line : lines) {
            assertTrue(line.length() <= 76,
                "chunked Base64 lines must be at most 76 chars, got " + line.length());
        }

        // Pin the trailing CRLF: the END marker must start a fresh line, not
        // be glued onto the last Base64 line. JDK's Base64.getMimeEncoder()
        // omits the trailing CRLF that commons-codec adds, so chunkedBase64()
        // appends it explicitly — this assertion is what catches a regression.
        assertTrue(letter.contains("\r\n-----END CERTIFICATE-----")
                || letter.contains("\n-----END CERTIFICATE-----"),
            "END CERTIFICATE marker must start on a new line");
    }

    @Test
    void letterHashIsUppercaseHex() throws Exception {
        User user = testUser();
        String letter = renderA005Letter(user);

        String expectedUpper = upperHex(
            MessageDigest.getInstance("SHA-256").digest(user.getA005Certificate()));

        String despaced = letter.replaceAll("\\s", "");
        assertTrue(despaced.contains(expectedUpper),
            "letter must contain UPPERCASE hex SHA-256 of the DER certificate");

        // Must not contain the lowercase form — pins case sensitivity that
        // existing InitLetterHashTest doesn't enforce.
        String expectedLower = expectedUpper.toLowerCase(Locale.ROOT);
        assertFalse(despaced.contains(expectedLower),
            "letter must use uppercase hex (lowercase form leaked in)");
    }

    @Test
    void decodeHexRoundTripsLowercaseHex() throws Exception {
        TestableInitElement element = new TestableInitElement();
        byte[] original = new byte[] {0x00, 0x1f, (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe};
        String hexLower = upperHex(original).toLowerCase(Locale.ROOT);

        byte[] decoded = element.decodeHexForTest(hexLower.getBytes(StandardCharsets.US_ASCII));

        assertEquals(upperHex(original), upperHex(decoded));
    }

    @Test
    void decodeHexThrowsEbicsExceptionOnInvalidInput() throws Exception {
        TestableInitElement element = new TestableInitElement();
        assertThrows(EbicsException.class,
            () -> element.decodeHexForTest("zz".getBytes(StandardCharsets.US_ASCII)));
    }

    // ----- helpers -----

    private static RSAPublicKey testKey() throws Exception {
        // MSB-set 2048-bit modulus so BigInteger.toByteArray() yields a leading
        // 0x00 sign byte; KeyUtil.getKeyDigest strips that first byte.
        StringBuilder mod = new StringBuilder("C0");
        for (int i = 0; i < 255; i++) {
            mod.append(String.format("%02X", i));
        }
        BigInteger modulus = new BigInteger(mod.toString(), 16);
        BigInteger exponent = BigInteger.valueOf(65537);
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
            .generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }

    private static User testUser() throws Exception {
        Bank bank = new Bank(new URL("https://bank.example/ebics"), "Test Bank", "HOSTID");
        Partner partner = new Partner(bank, "PARTNERID");
        return new User(partner, "USERID", "John Doe", "john@example.com", "DE", "ACME",
            "changeit"::toCharArray);
    }

    private static String renderA005Letter(User user) throws Exception {
        InitLetter letter = new A005Letter(Locale.ENGLISH);
        letter.create(user);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        letter.writeTo(out);
        return out.toString();
    }

    private static String upperHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xff));
        }
        return sb.toString();
    }

    /** Exposes the protected decodeHex for direct testing. */
    private static final class TestableInitElement extends InitializationRequestElement {
        TestableInitElement() {
            super((EbicsSession) null, (EbicsOrderType) null, "test");
        }

        @Override
        public void buildInitialization() {
            // not used
        }

        byte[] decodeHexForTest(byte[] hex) throws EbicsException {
            return decodeHex(hex);
        }
    }
}
