package org.ebics.client.http

interface HttpClientFactory {
    /**
     * Get preconfigured HTTP reusable client
     */
    fun getHttpClient(configurationName: String): HttpClient
}