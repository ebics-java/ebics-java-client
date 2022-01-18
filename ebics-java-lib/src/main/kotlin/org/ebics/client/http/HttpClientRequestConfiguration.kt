package org.ebics.client.http

interface HttpClientRequestConfiguration {
    val displayName: String
    /**
     * Specific HTTP header (if null then no header will be provided)
     * Example: "text/xml; charset=ISO-8859-1"
     */
    val httpContentType: String?
}