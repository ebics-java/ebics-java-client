package org.kopi.ebics.io;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.xml.UploadTransferRequestElement;
import org.mockito.Mockito;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitterTest {

    private static final int ONE_KB = 1000;
    private static final int ONE_MB = ONE_KB * ONE_KB;
    private static final int CHUNK_SIZE_LIMIT = 700 * ONE_KB;
    private static final int REQUEST_SIZE_LIMIT = 1000 * ONE_KB;
    private static final Random RANDOM_SEED = new Random(0);

    @Test
    void testSplitOneMb() throws NoSuchAlgorithmException, InvalidKeySpecException, EbicsException, IOException {
        byte[] b = randomDataArray(ONE_MB);
        Splitter splitter = splitAndVerifyContent(b);
        verifyActualRequestSize(splitter);
    }

    @Test
    void testSplitTwoMb() throws NoSuchAlgorithmException, InvalidKeySpecException, EbicsException, IOException {
        byte[] b = randomDataArray(2 * ONE_MB);
        Splitter splitter = splitAndVerifyContent(b);
        verifyActualRequestSize(splitter);
    }

    @Test
    void testSplitTenMb() throws NoSuchAlgorithmException, InvalidKeySpecException, EbicsException, IOException {
        byte[] b = randomDataArray(10 * ONE_MB);
        Splitter splitter = splitAndVerifyContent(b);
        verifyActualRequestSize(splitter);
    }

    @Test
    void testSplitFiftyMb() throws NoSuchAlgorithmException, InvalidKeySpecException, EbicsException, IOException {
        byte[] b = randomDataArray(50 * ONE_MB);
        Splitter splitter = splitAndVerifyContent(b);
        verifyActualRequestSize(splitter);
    }

    private UploadTransferRequestElement prepareActualRequest(Splitter splitter) throws EbicsException {
        EbicsSession ebicsSession = Mockito.mock(EbicsSession.class, Mockito.RETURNS_DEEP_STUBS);

        UploadTransferRequestElement uploader = new UploadTransferRequestElement(ebicsSession,
                OrderType.CDD,
                1,
                false,
                "asda".getBytes(),
                splitter.getContent(1)
        );
        uploader.build();
        return uploader;
    }

    private void verifyActualRequestSize(Splitter splitter) throws EbicsException {
        UploadTransferRequestElement fullRequest = prepareActualRequest(splitter);
        int actualSize = fullRequest.prettyPrint().length;
        assertTrue(actualSize < SplitterTest.REQUEST_SIZE_LIMIT);
    }

    private Splitter splitAndVerifyContent(byte[] b) throws InvalidKeySpecException, NoSuchAlgorithmException, EbicsException, IOException {
        Splitter splitter = new Splitter(b);
        splitter.readInput(true, new SecretKeySpec(secretKey().getEncoded(), "EAS"));
        int segmentSize = splitter.getSegmentSize();
        int segmentCount = splitter.getSegmentNumber();
        assertTrue(segmentSize < SplitterTest.CHUNK_SIZE_LIMIT);
        for (int i = 1; i <= segmentCount; i++) {
            int contentLength = splitter.getContent(i).getContent().available();
            assertTrue(contentLength < SplitterTest.CHUNK_SIZE_LIMIT);
        }
        return splitter;
    }

    private SecretKey secretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Security.addProvider(new BouncyCastleProvider());

        final String password = "test";
        int pswdIterations = 65536;
        int keySize = 256;
        byte[] saltBytes = {0, 1, 2, 3, 4, 5, 6};

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                saltBytes,
                pswdIterations,
                keySize
        );

        return factory.generateSecret(spec);
    }

    private byte[] randomDataArray(int size) {
        byte[] b = new byte[size];
        RANDOM_SEED.nextBytes(b);
        return b;
    }
}
