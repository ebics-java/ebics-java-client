/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.kopi.ebics.client;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.kopi.ebics.interfaces.Configuration;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.io.ByteArrayContentFactory;
import org.kopi.ebics.session.EbicsSession;

/**
 * A simple HTTP request sender and receiver. The send returns a HTTP code that
 * should be analyzed before proceeding ebics request response parse.
 *
 */
public class HttpRequestSender {

    private static final Duration TIMEOUT = Duration.ofSeconds(300);
    private static final String CONTENT_TYPE = "text/xml; charset=ISO-8859-1";

    private final EbicsSession session;
    private ContentFactory response;
    private final HttpClient httpClient;

    /**
     * Constructs a new <code>HttpRequestSender</code> with a given ebics
     * session.
     *
     * @param session the ebics session
     */
    public HttpRequestSender(EbicsSession session) {
        this.session = session;
        this.httpClient = createClient();
    }

    private HttpClient createClient() {
        HttpClient.Builder builder = HttpClient.newBuilder().connectTimeout(TIMEOUT);
        Configuration conf = session.getConfiguration();
        String proxyHost = conf.getProperty("http.proxy.host");

        if (proxyHost != null && !proxyHost.isEmpty()) {
            int proxyPort = Integer.parseInt(conf.getProperty("http.proxy.port").trim());
            builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost.trim(), proxyPort)));

            String user = conf.getProperty("http.proxy.user");
            if (user != null && !user.isEmpty()) {
                String trimmedUser = user.trim();
                String pwd = conf.getProperty("http.proxy.password").trim();
                builder.authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        // Only answer proxy challenges — never leak proxy
                        // credentials to a server-side 401.
                        if (getRequestorType() != RequestorType.PROXY) {
                            return null;
                        }
                        return new PasswordAuthentication(trimmedUser, pwd.toCharArray());
                    }
                });
            }
        }
        return builder.build();
    }

    /**
     * Sends the request contained in the <code>ContentFactory</code>. The
     * <code>ContentFactory</code> will deliver the request as an
     * <code>InputStream</code>.
     *
     * @param request the ebics request
     * @return the HTTP return code
     */
    public final int send(ContentFactory request) throws IOException {
        URI uri = URI.create(session.getUser().getPartner().getBank().getURL().toString());
        HttpRequest httpRequest = HttpRequest.newBuilder(uri)
            .timeout(TIMEOUT)
            .header("Content-Type", CONTENT_TYPE)
            .POST(HttpRequest.BodyPublishers.ofInputStream(() -> {
                try {
                    return request.getContent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }))
            .build();

        HttpResponse<byte[]> httpResponse;
        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request interrupted", e);
        }
        this.response = new ByteArrayContentFactory(httpResponse.body());
        return httpResponse.statusCode();
    }

    /**
     * Returns the content factory of the response body
     *
     * @return the content factory of the response.
     */
    public ContentFactory getResponseBody() {
        return response;
    }
}
