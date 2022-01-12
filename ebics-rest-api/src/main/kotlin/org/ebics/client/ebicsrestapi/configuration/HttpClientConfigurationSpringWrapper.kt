package org.ebics.client.ebicsrestapi.configuration

import org.ebics.client.http.HttpClientConfiguration

class HttpClientConfigurationSpringWrapper(
    override var sslTrustedStoreFile: String? = null,
    override var sslTrustedStoreFilePassword: String?  = null,
    override var httpProxyHost: String? = null,
    override var httpProxyPort: Int? = null,
    override var httpProxyUser: String? = null,
    override var httpProxyPassword: String? = null,
    override var httpContentType: String? = null,
    override var socketTimeoutMilliseconds: Int = 300 * 1000,
    override var connectionTimeoutMilliseconds: Int = 300 * 1000
) : HttpClientConfiguration