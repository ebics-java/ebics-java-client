package org.ebics.client.http

import org.ebics.client.api.EbicsSession

data class HttpTransferSession(override val session: EbicsSession) : IHttpTransferSession
