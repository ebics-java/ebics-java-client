package org.ebics.client.api.trace

import org.ebics.client.interfaces.EbicsRootElement
import org.ebics.client.api.TraceManager
import org.ebics.client.api.user.User
import org.ebics.client.api.EbicsSession
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class TraceService(
    private val traceRepository: TraceRepository,
    private var traceEnabled: Boolean = true
) : TraceManager {
    override fun trace(element: EbicsRootElement, session: EbicsSession) {
        if (traceEnabled) {
            val user = session.user as User
            traceRepository.save(TraceEntry(null, element.toString(), user))
        }
    }

    override fun setTraceEnabled(enabled: Boolean) {
        traceEnabled = enabled
    }
}