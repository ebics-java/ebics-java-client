package org.ebics.client.http

import org.ebics.client.api.EbicsSession
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory

interface IHttpTransferSession {
    val session: EbicsSession
    fun send(content: ContentFactory): ByteArrayContentFactory {
        with (session.user.partner.bank) {
            return session.configuration.httpClientFactory.getHttpClient(httpClientConfigurationName)
                .send(HttpClientRequest(bankURL, content))
        }
    }
}