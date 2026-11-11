package org.kopi.ebics.utils;

import org.junit.jupiter.api.Test;
import org.kopi.ebics.exception.EbicsException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class UtilsTest {

    @Test
    void rejectsTruncatedZlibStream() throws EbicsException {
        byte[] compressed = Utils.zip("download payload".getBytes(StandardCharsets.UTF_8));
        byte[] truncated = Arrays.copyOf(compressed, compressed.length - 1);

        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
                assertThrows(EbicsException.class, () -> Utils.unzip(truncated)));
    }

    @Test
    void rejectsPresetDictionary() throws Exception {
        byte[] payload = "download payload".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        Deflater compressor = new Deflater();
        try {
            compressor.setDictionary(payload);
            try (DeflaterOutputStream stream = new DeflaterOutputStream(compressed, compressor)) {
                stream.write(payload);
            }
        } finally {
            compressor.end();
        }

        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
                assertThrows(EbicsException.class, () -> Utils.unzip(compressed.toByteArray())));
    }

    @Test
    void roundTripsPayloadAcrossBuffers() throws EbicsException {
        byte[] payload = "download payload".repeat(200).getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(payload, Utils.unzip(Utils.zip(payload)));
    }

    @Test
    void roundTripsEmptyPayload() throws EbicsException {
        assertArrayEquals(new byte[0], Utils.unzip(Utils.zip(new byte[0])));
    }
}
