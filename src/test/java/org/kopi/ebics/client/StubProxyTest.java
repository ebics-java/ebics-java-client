package org.kopi.ebics.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class StubProxyTest {

    @Test
    void waitsForCompleteRequestBodyBeforeResponding() throws Exception {
        for (String framedBody : List.of(
                "Content-Length: 2\r\n\r\nab",
                "Transfer-Encoding: chunked\r\n\r\n1\r\na\r\n1\r\nb\r\n0\r\n\r\n")) {
            try (StubProxy proxy = new StubProxy();
                 Socket client = new Socket(InetAddress.getLoopbackAddress(), proxy.port())) {
                proxy.enqueueResponse(200, Map.of(), new byte[0]);
                var out = client.getOutputStream();
                var in = client.getInputStream();
                String request = "POST http://bank.example/ebics HTTP/1.1\r\n" + framedBody;
                // Withhold the last body byte (or final chunk terminator byte).
                out.write(request.substring(0, request.length() - 1).getBytes(StandardCharsets.ISO_8859_1));
                out.flush();
                client.setSoTimeout(500);
                assertThrows(SocketTimeoutException.class, in::read,
                    "proxy must not respond before the request body is complete");

                out.write(request.charAt(request.length() - 1));
                out.flush();
                client.setSoTimeout(5000);
                assertEquals("HTTP/1.1 200 Status\r\nContent-Length: 0\r\nConnection: close\r\n\r\n",
                    new String(in.readAllBytes(), StandardCharsets.ISO_8859_1));
            }
        }
    }
}
