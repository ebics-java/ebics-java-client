package org.kopi.ebics.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Minimal stub HTTP/1.1 proxy backed by a raw {@link ServerSocket}: records
 * each incoming request (request line + headers) and serves canned responses
 * from a queue. Used to exercise {@link HttpRequestSender}'s proxy + proxy-auth
 * paths without depending on a real proxy implementation.
 *
 * <p>Does not forward to any upstream server — every request is "answered"
 * locally, which is enough for asserting that the sender contacted the proxy
 * and supplied the right {@code Proxy-Authorization} header.
 */
final class StubProxy implements AutoCloseable {

    private final ServerSocket socket;
    private final Thread acceptor;
    private final List<RecordedRequest> requests = Collections.synchronizedList(new ArrayList<>());
    private final BlockingQueue<byte[]> responses = new LinkedBlockingQueue<>();

    StubProxy() throws IOException {
        this.socket = new ServerSocket(0, 16, InetAddress.getLoopbackAddress());
        this.acceptor = new Thread(this::acceptLoop, "stub-proxy-accept");
        this.acceptor.setDaemon(true);
        this.acceptor.start();
    }

    int port() {
        return socket.getLocalPort();
    }

    void enqueueResponse(int status, Map<String, String> headers, byte[] body) {
        StringBuilder head = new StringBuilder();
        head.append("HTTP/1.1 ").append(status).append(" Status\r\n");
        for (Map.Entry<String, String> e : headers.entrySet()) {
            head.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
        }
        head.append("Content-Length: ").append(body.length).append("\r\n");
        head.append("Connection: close\r\n");
        head.append("\r\n");
        byte[] headBytes = head.toString().getBytes(StandardCharsets.ISO_8859_1);
        byte[] full = new byte[headBytes.length + body.length];
        System.arraycopy(headBytes, 0, full, 0, headBytes.length);
        System.arraycopy(body, 0, full, headBytes.length, body.length);
        responses.add(full);
    }

    List<RecordedRequest> recordedRequests() {
        synchronized (requests) {
            return new ArrayList<>(requests);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    private void acceptLoop() {
        while (!socket.isClosed()) {
            try {
                Socket client = socket.accept();
                Thread handler = new Thread(() -> handle(client), "stub-proxy-handle");
                handler.setDaemon(true);
                handler.start();
            } catch (IOException e) {
                // socket closed → loop exits
                return;
            }
        }
    }

    private void handle(Socket client) {
        try (client; InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream()) {
            RecordedRequest req = parseRequest(in);
            requests.add(req);
            byte[] response = responses.poll(5, TimeUnit.SECONDS);
            if (response == null) {
                response = "HTTP/1.1 500 No canned response\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
                    .getBytes(StandardCharsets.ISO_8859_1);
            }
            out.write(response);
            out.flush();
        } catch (Exception ignored) {
            // Test failure will surface via assertions on the recorded requests.
        }
    }

    private static RecordedRequest parseRequest(InputStream in) throws IOException {
        String requestLine = readLine(in);
        Map<String, String> headers = new LinkedHashMap<>();
        String line;
        while (!(line = readLine(in)).isEmpty()) {
            int colon = line.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String name = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            headers.put(name.toLowerCase(Locale.ROOT), value);
        }
        // Body is intentionally not drained: for our assertions only the
        // request line + headers matter, and HttpClient is happy as long as
        // it eventually sees a complete response on this connection.
        return new RecordedRequest(requestLine, headers);
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int prev = -1;
        int ch;
        while ((ch = in.read()) != -1) {
            if (prev == '\r' && ch == '\n') {
                byte[] b = buf.toByteArray();
                return new String(b, 0, b.length - 1, StandardCharsets.ISO_8859_1);
            }
            buf.write(ch);
            prev = ch;
        }
        return new String(buf.toByteArray(), StandardCharsets.ISO_8859_1);
    }

    record RecordedRequest(String requestLine, Map<String, String> headers) {
        String header(String name) {
            return headers.get(name.toLowerCase(Locale.ROOT));
        }
    }
}
