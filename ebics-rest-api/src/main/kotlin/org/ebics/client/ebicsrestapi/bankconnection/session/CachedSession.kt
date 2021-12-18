package org.ebics.client.ebicsrestapi.bankconnection.session

import org.ebics.client.api.EbicsSession

data class CachedSession(val sessionId: String, val session: EbicsSession)
