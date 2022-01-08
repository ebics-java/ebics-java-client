package org.ebics.client.http

interface HttpClientConfiguration {
    /**
     * The SSL trusted store, used for establishing connections if needed (usually for no public EBICS servers only).
     */
    val sslTrustedStoreFile: String?
    val sslTrustedStoreFilePassword: String?

    /**
     * HTTP Proxy, with optional technical user/password
     */
    val httpProxyHost: String?
    val httpProxyPort: Int?
    val httpProxyUser: String?
    val httpProxyPassword: String?

    /**
     * Specific HTTP header (if null then no header will be provided)
     * Example: "text/xml; charset=ISO-8859-1"
     */
    val httpContentHeader: String?

    /**
     * Timeouts in milli-seconds (or -1)
     * Default suggested value 300.000 = 300s
     */
    val socketTimeoutMilliseconds: Int
    val connectionTimeoutMilliseconds: Int
}