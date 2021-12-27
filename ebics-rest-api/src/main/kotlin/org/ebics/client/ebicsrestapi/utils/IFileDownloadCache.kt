package org.ebics.client.ebicsrestapi.utils

import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.EbicsVersion

interface IFileDownloadCache {
    fun getLastFileCached(
        session: EbicsSession,
        orderType: OrderTypeDefinition,
        ebicsVersion: EbicsVersion,
        useCache: Boolean = true,
    ): ByteArray

    fun retrieveFileOnlineAndStoreToCache(
        session: EbicsSession,
        orderType: OrderTypeDefinition,
        ebicsVersion: EbicsVersion
    ): ByteArray
}