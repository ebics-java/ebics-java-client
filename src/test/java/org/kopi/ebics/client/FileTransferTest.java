package org.kopi.ebics.client;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.stream.Collectors;

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
        assertDirectoryContains(destination);
    }

    @Test
    void leavesNewDestinationAbsentOnDecompressionFailure() throws Exception {
        Path destination = directory.resolve("download.dat");
        when(session.getUser().decrypt(any(byte[].class), any(byte[].class)))
            .thenReturn(new byte[] {0, 0});

        assertThrows(EbicsException.class, () -> fetchFile(destination));

        assertFalse(Files.exists(destination));
        assertDirectoryContains();
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
        assertDirectoryContains(destination);
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
        assertDirectoryContains(destination);
    }

    @Test
    void preservesDestinationWhenAtomicMoveIsUnsupported() throws Exception {
        Path destination = directory.resolve("download.dat");
        byte[] original = "existing content".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, original);
        when(session.getUser().decrypt(any(byte[].class), any(byte[].class)))
            .thenReturn(Utils.zip("download payload".getBytes(StandardCharsets.UTF_8)));
        var failure = new AtomicMoveNotSupportedException("temporary file", "destination", "unsupported");

        try (var files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.move(any(Path.class), eq(destination),
                eq(REPLACE_EXISTING), eq(ATOMIC_MOVE))).thenThrow(failure);

            assertSame(failure, assertThrows(AtomicMoveNotSupportedException.class,
                () -> fetchFile(destination)));
        }

        assertArrayEquals(original, Files.readAllBytes(destination));
        assertDirectoryContains(destination);
    }

    @Test
    void preservesDestinationOnPartialWriteFailure() throws Exception {
        Path destination = directory.resolve("download.dat");
        byte[] original = "existing content".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, original);
        when(session.getUser().decrypt(any(byte[].class), any(byte[].class)))
            .thenReturn(Utils.zip("download payload".getBytes(StandardCharsets.UTF_8)));
        var failure = new IOException("synthetic disk full");

        try (var files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.newOutputStream(any(Path.class))).thenAnswer(invocation ->
                new FilterOutputStream((OutputStream) invocation.callRealMethod()) {
                    @Override
                    public void write(byte[] bytes, int offset, int length) throws IOException {
                        out.write(bytes, offset, Math.min(3, length));
                        throw failure;
                    }
                });

            assertThrows(EbicsException.class, () -> fetchFile(destination));
        }

        assertArrayEquals(original, Files.readAllBytes(destination));
        assertDirectoryContains(destination);
    }

    @Test
    void preservesDestinationOnStreamCloseFailure() throws Exception {
        Path destination = directory.resolve("download.dat");
        byte[] original = "existing content".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, original);
        when(session.getUser().decrypt(any(byte[].class), any(byte[].class)))
            .thenReturn(Utils.zip("download payload".getBytes(StandardCharsets.UTF_8)));
        var failure = new IOException("synthetic close failure");

        try (var files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.newOutputStream(any(Path.class))).thenAnswer(invocation ->
                new FilterOutputStream((OutputStream) invocation.callRealMethod()) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        throw failure;
                    }
                });

            assertSame(failure, assertThrows(IOException.class, () -> fetchFile(destination)));
        }

        assertArrayEquals(original, Files.readAllBytes(destination));
        assertDirectoryContains(destination);
    }

    @Test
    void downloadsToBareRelativeFilename() throws Exception {
        Path destination = Files.createTempFile(Path.of("."), "ebics-relative-", ".dat").getFileName();
        try {
            Files.delete(destination);
            byte[] payload = "download payload".getBytes(StandardCharsets.UTF_8);
            when(session.getUser().decrypt(any(byte[].class), any(byte[].class)))
                .thenReturn(Utils.zip(payload));

            fetchFile(destination);

            assertArrayEquals(payload, Files.readAllBytes(destination));
        } finally {
            Files.deleteIfExists(destination);
        }
    }

    private void assertDirectoryContains(Path... expected) throws Exception {
        try (var entries = Files.list(directory)) {
            assertEquals(Set.of(expected), entries.collect(Collectors.toSet()));
        }
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
            try {
                new FileTransfer(session).fetchFile(OrderType.C53, destination.toFile());
            } catch (Exception failure) {
                assertEquals(0, receipts.constructed().size());
                verify(senders.constructed().get(0), times(1)).send(any());
                throw failure;
            }
        }
    }
}
