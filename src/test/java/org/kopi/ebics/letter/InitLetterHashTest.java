package org.kopi.ebics.letter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.Security;
import java.util.HexFormat;
import java.util.Locale;

import org.apache.xml.security.Init;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.kopi.ebics.client.Bank;
import org.kopi.ebics.client.Partner;
import org.kopi.ebics.client.User;
import org.kopi.ebics.interfaces.InitLetter;

/**
 * Verifies that the INI and HIA letters carry the SHA-256 hash of the
 * DER-encoded certificate, as required by EBICS 3.0 (H005), spec ch. 4.4.1.2.3.
 *
 * <p>Before this was fixed the letters printed the SHA-256 of the public key
 * (the EBICS 2.5 form), which made the bank-side letter verification fail even
 * though the INI/HIA request always transmits the X.509 certificate.
 */
class InitLetterHashTest {
    static {
        Init.init();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void lettersContainCertificateHash() throws Exception {
        User user = testUser();

        assertLetterContainsCertificateHash(new A005Letter(Locale.ENGLISH), user,
            user.getA005Certificate());
        assertLetterContainsCertificateHash(new E002Letter(Locale.ENGLISH), user,
            user.getE002Certificate());
        assertLetterContainsCertificateHash(new X002Letter(Locale.ENGLISH), user,
            user.getX002Certificate());
    }

    private void assertLetterContainsCertificateHash(InitLetter letter, User user, byte[] der)
        throws Exception {
        letter.create(user);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        letter.writeTo(out);
        // The hash is printed grouped into space-separated pairs across two
        // lines; strip whitespace so we can match the contiguous hex digest.
        String despaced = out.toString().replaceAll("\\s", "").toUpperCase(Locale.ROOT);

        String expected = HexFormat.of().withUpperCase().formatHex(
            MessageDigest.getInstance("SHA-256").digest(der));

        assertTrue(despaced.contains(expected),
            letter.getClass().getSimpleName()
                + " must print the SHA-256 hash of the DER-encoded certificate");
    }

    private User testUser() throws Exception {
        Bank bank = new Bank(new URL("https://bank.example/ebics"), "Test Bank", "HOSTID");
        Partner partner = new Partner(bank, "PARTNERID");
        return new User(partner, "USERID", "John Doe", "john@example.com", "DE", "ACME",
            "changeit"::toCharArray);
    }
}
