package org.ebics.client.http

interface HttpClientGlobalConfiguration {
    val connectionPoolMaxTotal: Int
    val connectionPoolDefaultMaxPerRoute: Int
    val configurations: Map<String, HttpClientConfiguration>
}