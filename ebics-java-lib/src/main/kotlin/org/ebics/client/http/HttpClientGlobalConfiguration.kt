package org.ebics.client.http

interface HttpClientGlobalConfiguration {
    val connectionPoolMaxTotal: Int
    val connectionPoolDefaultMaxPerRoute: Int
    val namedClientConfigurations: Map<String, HttpClientConfiguration>
}