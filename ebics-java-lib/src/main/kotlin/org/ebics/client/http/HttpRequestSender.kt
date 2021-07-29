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
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.ProxyAuthenticationStrategy
import org.apache.http.ssl.SSLContexts
import org.apache.http.util.EntityUtils
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
 */
/**
 * Constructs a new `HttpRequestSender` with a given ebics
 * session.
 *
 * @param session
 * the ebics session
 */
class HttpRequestSender(private val session: EbicsSession) {

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
        val configBuilder = RequestConfig.copy(RequestConfig.DEFAULT).setSocketTimeout(300000).setConnectTimeout(300000)
        val conf = session.configuration

        val builder = HttpClientBuilder.create().setDefaultRequestConfig(
            configBuilder.build()
        )

        with(conf) {
            val port = httpProxyPort
            if (httpProxyHost.isNullOrBlank() && port != null) {
                configBuilder.setProxy(HttpHost(httpProxyHost, port))
                if (!httpProxyUser.isNullOrBlank()) {
                    val credentialsProvider = BasicCredentialsProvider()
                    credentialsProvider.setCredentials(
                        AuthScope(httpProxyHost, port),
                        UsernamePasswordCredentials(conf.httpProxyUser, httpProxyPassword)
                    )
                    builder.setDefaultCredentialsProvider(credentialsProvider)
                    builder.setProxyAuthenticationStrategy(ProxyAuthenticationStrategy())
                }
            }
        }
        if (!conf.sslTrustedStoreFile.isNullOrBlank()) {
            val trustStoreFile = File(conf.sslTrustedStoreFile)
            if (trustStoreFile.exists()) {
                try {
                    builder.setSSLContext(SSLContexts.custom().loadTrustMaterial(trustStoreFile).build())
                } catch (e: Exception) {
                    logger.error("Error loading truststore: " + e.message)
                    e.printStackTrace()
                }
            }
        }
        val httpClient = builder.build()
        val input = request.content
        val method = HttpPost(session.user.partner.bank.bankURL.toString())
        val requestEntity = EntityBuilder.create().setStream(input).build()
        method.entity = requestEntity
        method.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=ISO-8859-1")
        httpClient.execute(method).use { response ->
            //Check the HTTP return code (must be 200)
            Utils.checkHttpCode(response.statusLine.statusCode)
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