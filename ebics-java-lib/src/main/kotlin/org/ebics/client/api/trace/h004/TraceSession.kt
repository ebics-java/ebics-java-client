package org.ebics.client.api.trace.h004

import org.ebics.client.api.EbicsSession
import org.ebics.client.interfaces.EbicsRootElement
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.IOrderTypeDefinition25
import java.util.*

data class TraceSession(
    override val session: EbicsSession,
    override val orderType: IOrderTypeDefinition25,
    override val upload: Boolean = true,
    override val ebicsVersion: EbicsVersion = EbicsVersion.H004,
    override val orderNumber: String = UUID.randomUUID().toString()
) : ITraceSession {
    override fun trace(element: EbicsRootElement) {
        session.configuration.traceManager.trace(element, this)
    }
}