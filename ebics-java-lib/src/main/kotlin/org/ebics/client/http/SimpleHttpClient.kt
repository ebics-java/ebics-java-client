package org.ebics.client.http

import org.apache.http.HttpHeaders
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.utils.Utils
import org.slf4j.LoggerFactory
import javax.annotation.PreDestroy

class SimpleHttpClient(
    private val httpClient: CloseableHttpClient,
    override val configuration: HttpClientRequestConfiguration
) : HttpClient {
    override fun send(request: HttpClientRequest): ByteArrayContentFactory {
        logger.trace("Sending HTTP POST request to URL: ${request.requestURL}")
        val method = HttpPost(request.requestURL.toURI())
        method.entity = EntityBuilder.create().setStream(request.content.content).build()
        if (!configuration.httpContentType.isNullOrBlank()) {
            logger.trace("Setting HTTP POST content header: ${configuration.httpContentType}")
            method.setHeader(HttpHeaders.CONTENT_TYPE, configuration.httpContentType)
        }
        httpClient.execute(method).use { response ->
            logger.trace("Received HTTP POST response code ${response.statusLine.statusCode} from URL: ${request.requestURL}")
            //Check the HTTP return code (must be 200)
            Utils.checkHttpCode(response)
            //If ok returning content
            return ByteArrayContentFactory(
                EntityUtils.toByteArray(response.entity)
            )
        }
    }

    @PreDestroy
    override fun close() {
        logger.debug("Closing gracefully the HTTP client")
        httpClient.close()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PooledHttpClientFactory::class.java)
    }
}