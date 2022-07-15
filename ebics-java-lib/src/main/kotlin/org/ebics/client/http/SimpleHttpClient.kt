package org.ebics.client.http

import org.apache.http.HttpHeaders
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import org.ebics.client.exception.EbicsException
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.messages.Messages
import org.ebics.client.utils.Utils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger
import javax.annotation.PreDestroy

class SimpleHttpClient(
    private val httpClient: CloseableHttpClient,
    override val configuration: HttpClientRequestConfiguration,
    val configurationName: String = "default"
) : HttpClient {
    override fun send(request: HttpClientRequest): ByteArrayContentFactory {
        logger.trace("Sending HTTP POST request to URL: ${request.requestURL} using config: $configurationName")
        val method = HttpPost(request.requestURL.toURI())
        method.entity = EntityBuilder.create().setStream(request.content.content).build()
        if (!configuration.httpContentType.isNullOrBlank()) {
            logger.trace("Setting HTTP POST content header: ${configuration.httpContentType}")
            method.setHeader(HttpHeaders.CONTENT_TYPE, configuration.httpContentType)
        }
        httpClient.execute(method).use { response ->
            logger.trace("Received HTTP POST response code ${response.statusLine.statusCode} from URL: ${request.requestURL} using config: $configurationName")
            //Check the HTTP return code (must be 200)
            checkHttpCode(response)
            //If ok returning content
            return ByteArrayContentFactory(
                EntityUtils.toByteArray(response.entity)
            )
        }
    }

    /**
     * Checks for the returned http code
     *
     * @param response the http response
     * @throws EbicsException
     */
    @Throws(EbicsException::class)
    private fun checkHttpCode(response: CloseableHttpResponse) {
        val httpCode = response.statusLine.statusCode
        val reasonPhrase = response.statusLine.reasonPhrase
        if (httpCode != 200) {
            //Log detail response in server log
            try {
                val entity = response.entity
                val responseString = EntityUtils.toString(entity, "UTF-8")
                logger.warn(
                    "Unexpected HTTP Code: {0} returned as EBICS response, reason: {1}, response content: {2}",
                    httpCode, reasonPhrase, responseString
                )
                throw EbicsException(
                    String.format("Wrong returned HTTP code: %d %s, with the response content '%s'", httpCode, reasonPhrase, responseString)
                )
            } catch (e: IOException) {
                logger.warn(
                    "Unexpected HTTP Code: {0} returned as EBICS response, reason: {1}, response content can't be read",
                    httpCode, reasonPhrase
                )
                throw EbicsException(
                    String.format("Wrong returned HTTP code: %d %s (no response content available) ", httpCode, reasonPhrase)
                )
            }
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