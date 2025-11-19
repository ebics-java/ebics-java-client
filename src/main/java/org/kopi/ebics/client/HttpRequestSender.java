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
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.util.EntityUtils;
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

    private final EbicsSession session;
    private ContentFactory response;
    private final CloseableHttpClient httpClient;

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

    private CloseableHttpClient createClient() {
        RequestConfig.Builder configBuilder = RequestConfig.copy(RequestConfig.DEFAULT)
            .setSocketTimeout(300_000).setConnectTimeout(300_000);
        Configuration conf = session.getConfiguration();
        String proxyHost = conf.getProperty("http.proxy.host");
        CredentialsProvider credsProvider = null;

        if (proxyHost != null && !proxyHost.isEmpty()) {
            int proxyPort = Integer.parseInt(conf.getProperty("http.proxy.port").trim());
            HttpHost proxy = new HttpHost(proxyHost.trim(), proxyPort);
            configBuilder.setProxy(proxy);

            String user = conf.getProperty("http.proxy.user");
            if (user != null && !user.isEmpty()) {
                user = user.trim();
                String pwd = conf.getProperty("http.proxy.password").trim();
                credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
                    new UsernamePasswordCredentials(user, pwd));
            }
        }
        HttpClientBuilder builder = HttpClientBuilder.create()
            .setDefaultRequestConfig(configBuilder.build());
        if (credsProvider != null) {
            builder.setDefaultCredentialsProvider(credsProvider);
            builder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
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
        InputStream input = request.getContent();
        HttpPost method = new HttpPost(
            session.getUser().getPartner().getBank().getURL().toString());

        HttpEntity requestEntity = EntityBuilder.create().setStream(input).build();
        method.setEntity(requestEntity);
        method.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=ISO-8859-1");

        try (CloseableHttpResponse response = httpClient.execute(method)) {
            this.response = new ByteArrayContentFactory(
                EntityUtils.toByteArray(response.getEntity()));
            return response.getStatusLine().getStatusCode();
        }
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
