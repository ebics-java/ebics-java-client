package org.kopi.ebics.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kopi.ebics.io.ByteArrayContentFactory;
import org.kopi.ebics.session.EbicsSession;
import org.mockito.Mockito;

/**
 * End-to-end test for {@link HttpRequestSender}: spins up an in-process
 * {@link HttpServer}, captures what the sender posts, and asserts the
 * sender returns the server's response body and status code unmodified.
 *
 * <p>Written before migrating from Apache HttpClient 4.x to
 * {@link java.net.http.HttpClient} so the contract is pinned independently
 * of the underlying client.
 */
class HttpRequestSenderTest {

    private HttpServer server;
    private AtomicReference<String> capturedMethod;
    private AtomicReference<String> capturedContentType;
    private AtomicReference<byte[]> capturedBody;
    private volatile int responseStatus;
    private volatile byte[] responseBody;

    @BeforeEach
    void startServer() throws Exception {
        capturedMethod = new AtomicReference<>();
        capturedContentType = new AtomicReference<>();
        capturedBody = new AtomicReference<>();
        responseStatus = 200;
        responseBody = "<response/>".getBytes(StandardCharsets.UTF_8);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            capturedMethod.set(exchange.getRequestMethod());
            capturedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            capturedBody.set(exchange.getRequestBody().readAllBytes());
            exchange.sendResponseHeaders(responseStatus, responseBody.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsRequestBodyAndReturnsResponse() throws Exception {
        byte[] requestBody = "<EbicsRequest>hello</EbicsRequest>".getBytes(StandardCharsets.UTF_8);

        HttpRequestSender sender = new HttpRequestSender(session(serverUrl()));
        int status = sender.send(new ByteArrayContentFactory(requestBody));

        assertEquals(200, status, "status code must be propagated from server");
        assertEquals("POST", capturedMethod.get(), "must use POST");
        assertEquals("text/xml; charset=ISO-8859-1", capturedContentType.get(),
            "Content-Type header must match EBICS expectation");
        assertArrayEquals(requestBody, capturedBody.get(),
            "request body bytes must reach the server unchanged");
        assertArrayEquals(responseBody, sender.getResponseBody().getContent().readAllBytes(),
            "response body must be exposed via getResponseBody()");
    }

    @Test
    void propagatesNonSuccessStatusCodes() throws Exception {
        responseStatus = 500;
        responseBody = "<error/>".getBytes(StandardCharsets.UTF_8);

        HttpRequestSender sender = new HttpRequestSender(session(serverUrl()));
        int status = sender.send(new ByteArrayContentFactory("x".getBytes(StandardCharsets.UTF_8)));

        assertEquals(500, status);
        assertArrayEquals(responseBody, sender.getResponseBody().getContent().readAllBytes());
    }

    @Test
    void routesThroughConfiguredProxyWithoutAuth() throws Exception {
        try (StubProxy proxy = new StubProxy()) {
            proxy.enqueueResponse(200, Map.of(), "<ok/>".getBytes(StandardCharsets.UTF_8));

            EbicsSession session = proxiedSession(proxy.port(), null, null);
            HttpRequestSender sender = new HttpRequestSender(session);
            int status = sender.send(new ByteArrayContentFactory("x".getBytes(StandardCharsets.UTF_8)));

            assertEquals(200, status);
            List<StubProxy.RecordedRequest> reqs = proxy.recordedRequests();
            assertEquals(1, reqs.size(), "proxy must receive exactly one request");
            // Going through a proxy, the request line carries the absolute URI.
            assertTrue(reqs.get(0).requestLine().contains("http://bank.example/ebics"),
                "request line must contain the absolute target URI; got: " + reqs.get(0).requestLine());
            assertFalse(reqs.get(0).headers().containsKey("proxy-authorization"),
                "no Proxy-Authorization header expected when no credentials configured");
        }
    }

    @Test
    void retriesWithProxyAuthorizationAfter407Challenge() throws Exception {
        try (StubProxy proxy = new StubProxy()) {
            // First connection: challenge with 407 so the client invokes the Authenticator.
            proxy.enqueueResponse(407,
                Map.of("Proxy-Authenticate", "Basic realm=\"ebics\""),
                new byte[0]);
            // Second connection: the retry carrying Proxy-Authorization.
            proxy.enqueueResponse(200, Map.of(), "<ok/>".getBytes(StandardCharsets.UTF_8));

            EbicsSession session = proxiedSession(proxy.port(), "alice", "s3cret");
            HttpRequestSender sender = new HttpRequestSender(session);
            int status = sender.send(new ByteArrayContentFactory("x".getBytes(StandardCharsets.UTF_8)));

            assertEquals(200, status, "client must complete the request after auth retry");
            List<StubProxy.RecordedRequest> reqs = proxy.recordedRequests();
            assertEquals(2, reqs.size(), "proxy must see the original request plus the auth retry");
            assertFalse(reqs.get(0).headers().containsKey("proxy-authorization"),
                "first request must go without credentials (challenge-response flow)");

            String expectedAuth = "Basic "
                + Base64.getEncoder().encodeToString("alice:s3cret".getBytes(StandardCharsets.UTF_8));
            assertEquals(expectedAuth, reqs.get(1).header("Proxy-Authorization"),
                "retry must carry Basic Proxy-Authorization derived from configured credentials");
        }
    }

    private URL serverUrl() throws Exception {
        return new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/");
    }

    private static EbicsSession session(URL url) {
        EbicsSession session = Mockito.mock(EbicsSession.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(session.getConfiguration().getProperty(Mockito.anyString())).thenReturn(null);
        Mockito.when(session.getUser().getPartner().getBank().getURL()).thenReturn(url);
        return session;
    }

    private static EbicsSession proxiedSession(int proxyPort, String user, String pass) throws Exception {
        EbicsSession session = Mockito.mock(EbicsSession.class, Mockito.RETURNS_DEEP_STUBS);
        var conf = session.getConfiguration();
        Mockito.when(conf.getProperty(Mockito.anyString())).thenReturn(null);
        Mockito.when(conf.getProperty("http.proxy.host")).thenReturn("127.0.0.1");
        Mockito.when(conf.getProperty("http.proxy.port")).thenReturn(String.valueOf(proxyPort));
        if (user != null) {
            Mockito.when(conf.getProperty("http.proxy.user")).thenReturn(user);
            Mockito.when(conf.getProperty("http.proxy.password")).thenReturn(pass);
        }
        // Target host is arbitrary — the stub proxy intercepts everything and never forwards.
        Mockito.when(session.getUser().getPartner().getBank().getURL())
            .thenReturn(new URL("http://bank.example/ebics"));
        return session;
    }
}
