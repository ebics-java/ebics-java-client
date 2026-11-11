package org.kopi.ebics.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.utils.Utils;
import org.kopi.ebics.xml.DownloadInitializationRequestElement;
import org.kopi.ebics.xml.DownloadInitializationResponseElement;
import org.kopi.ebics.xml.ReceiptRequestElement;
import org.kopi.ebics.xml.ReceiptResponseElement;

class FileTransferTest {

    @TempDir
    Path directory;

    private final EbicsSession session = mock(EbicsSession.class, RETURNS_DEEP_STUBS);

    @Test
    void preservesExistingDestinationOnDecompressionFailure() throws Exception {
        Path destination = directory.resolve("download.dat");
        byte[] original = "existing content".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, original);
        // Invalid zlib header: fails immediately rather than waiting for more input.
        when(session.getUser().decrypt(any(byte[].class), any(byte[].class)))
            .thenReturn(new byte[] {0, 0});

        assertThrows(EbicsException.class, () -> fetchFile(destination));

        assertArrayEquals(original, Files.readAllBytes(destination));
    }

    @Test
    void leavesNewDestinationAbsentOnDecompressionFailure() throws Exception {
        Path destination = directory.resolve("download.dat");
        when(session.getUser().decrypt(any(byte[].class), any(byte[].class)))
            .thenReturn(new byte[] {0, 0});

        assertThrows(EbicsException.class, () -> fetchFile(destination));

        assertFalse(Files.exists(destination));
    }

    @Test
    void preservesExistingDestinationOnDecryptionFailure() throws Exception {
        Path destination = directory.resolve("download.dat");
        byte[] original = "existing content".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, original);
        when(session.getUser().decrypt(any(byte[].class), any(byte[].class)))
            .thenThrow(new GeneralSecurityException("synthetic decryption failure"));

        assertThrows(EbicsException.class, () -> fetchFile(destination));

        assertArrayEquals(original, Files.readAllBytes(destination));
    }

    @Test
    void replacesDestinationWithDecompressedPayload() throws Exception {
        Path destination = directory.resolve("download.dat");
        Files.writeString(destination, "old content that must be replaced completely");
        byte[] payload = "download payload".getBytes(StandardCharsets.UTF_8);
        when(session.getUser().decrypt(any(byte[].class), any(byte[].class)))
            .thenReturn(Utils.zip(payload));

        fetchFile(destination);

        assertArrayEquals(payload, Files.readAllBytes(destination));
    }

    private void fetchFile(Path destination) throws Exception {
        // Keep FileTransfer, Joiner, decompression and filesystem I/O real;
        // replace only the bank exchange and cryptographic boundary.
        try (var senders = mockConstruction(HttpRequestSender.class,
                 (sender, context) -> when(sender.send(any())).thenReturn(200));
             var initializers = mockConstruction(DownloadInitializationRequestElement.class);
             var responses = mockConstruction(DownloadInitializationResponseElement.class,
                 (response, context) -> {
                     when(response.getSegmentsNumber()).thenReturn(1);
                     when(response.getSegmentNumber()).thenReturn(1);
                     when(response.getTransactionId()).thenReturn(new byte[16]);
                     when(response.getTransactionKey()).thenReturn(new byte[16]);
                     when(response.getOrderData()).thenReturn(new byte[] {1});
                 });
             var receipts = mockConstruction(ReceiptRequestElement.class);
             var receiptResponses = mockConstruction(ReceiptResponseElement.class)) {
            new FileTransfer(session).fetchFile(OrderType.C53, destination.toFile());
        }
    }
}
