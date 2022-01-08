package org.ebics.client.http

import org.ebics.client.interfaces.ContentFactory
import java.net.URL

data class HttpClientRequest (
    val requestURL: URL,
    val content: ContentFactory
)