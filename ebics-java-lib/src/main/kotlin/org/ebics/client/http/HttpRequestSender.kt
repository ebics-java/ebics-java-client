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
 * $Id$
 */
package org.ebics.client.http

import org.apache.http.HttpHeaders
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.ProxyAuthenticationStrategy
import org.apache.http.ssl.SSLContexts
import org.apache.http.util.EntityUtils
import org.ebics.client.api.Configuration
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.api.EbicsSession
import org.ebics.client.utils.Utils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * A simple HTTP request sender and receiver. The send returns a HTTP code that
 * should be analyzed before proceeding ebics request response parse.
 *
 * Constructs a new `HttpRequestSender` with a given ebics
 * session.
 *
 * @param session
 * the ebics session
 */
class HttpRequestSender(private val configuration: Configuration, private val bankURL: String) {

    constructor(session: EbicsSession) : this(session.configuration, session.user.partner.bank.bankURL.toString())

    /**
     * Create HTTP client from EBICS configuration
     */
    private fun createHttpClient(conf: Configuration): CloseableHttpClient {
        with(conf) {
            with(HttpClientBuilder.create()) {
                with (RequestConfig.copy(RequestConfig.DEFAULT)) {
                    setSocketTimeout(300000)
                    setConnectTimeout(300000)
                    if (httpProxyHost.isNullOrBlank() && httpProxyPort != null) {
                        setProxy(HttpHost(httpProxyHost, httpProxyPort!!))
                    }
                    setDefaultRequestConfig(build())
                }

                if (!httpProxyUser.isNullOrBlank()) {
                    val credentialsProvider = BasicCredentialsProvider().apply {
                        setCredentials(
                            AuthScope(httpProxyHost, httpProxyPort!!),
                            UsernamePasswordCredentials(httpProxyUser, httpProxyPassword)
                        )
                    }
                    setDefaultCredentialsProvider(credentialsProvider)
                    setProxyAuthenticationStrategy(ProxyAuthenticationStrategy())
                }

                if (!sslTrustedStoreFile.isNullOrBlank()) {
                    val trustStoreFile = File(sslTrustedStoreFile!!)
                    if (trustStoreFile.exists()) {
                        try {
                            setSSLContext(SSLContexts.custom().loadTrustMaterial(trustStoreFile).build())
                        } catch (e: Exception) {
                            logger.error("Error loading truststore: ", e)
                        }
                    } else
                        logger.error("Provided truststore file name doesn't exist: $sslTrustedStoreFile")
                }
                return build()
            }
        }
    }

    /**
     *  Sends the request contained in the `ContentFactory`.
     *  The ContentFactory` will deliver the request as an
     * `InputStream`.
     *  The response code is checked on OK
     *
     * @param request
     * the ebics request
     * @return the content factory of the response.
     */
    @Throws(IOException::class)
    fun send(request: ContentFactory): ByteArrayContentFactory {
        val httpClient = createHttpClient(configuration)
        val method = HttpPost(bankURL)
        val requestEntity = EntityBuilder.create().setStream(request.content).build()
        method.entity = requestEntity
        method.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=ISO-8859-1")
        httpClient.execute(method).use { response ->
            //Check the HTTP return code (must be 200)
            Utils.checkHttpCode(response)
            //If ok returning content
            return ByteArrayContentFactory(
                EntityUtils.toByteArray(response.entity)
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HttpRequestSender::class.java)
    }
}