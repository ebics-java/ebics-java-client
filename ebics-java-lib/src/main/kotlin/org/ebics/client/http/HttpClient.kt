package org.ebics.client.http

import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import java.net.URL

interface HttpClient {

    /**
     * Actual configuration of the client
     */
    val configuration: HttpClientRequestConfiguration

    /**
     * Send HTTP request to given URL, using header if indicated
     * Return response bytes if the code is HTTP_OK (200),
     * Otherwise throw exception
     */
    fun send(request: HttpClientRequest): ByteArrayContentFactory

    fun send(
        requestURL: URL,
        content: ContentFactory
    ) = send(HttpClientRequest(requestURL, content))

    /**
     * Gracefully close the client
     */
    fun close()
}