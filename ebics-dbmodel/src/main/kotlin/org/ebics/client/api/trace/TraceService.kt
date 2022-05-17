package org.ebics.client.api.trace

import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.trace.orderType.EbicsService
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.interfaces.EbicsRootElement
import org.springframework.stereotype.Service

@Service
class TraceService(
    private val traceRepository: TraceRepository,
    private var traceEnabled: Boolean = true
) : TraceManager {

    override fun trace(element: EbicsRootElement, traceSession: org.ebics.client.api.trace.h004.ITraceSession) {
        if (traceEnabled) {
            with(traceSession) {
                val orderType =
                    OrderTypeDefinition(orderType.adminOrderType, businessOrderType = orderType.businessOrderType)
                trace(element, orderType, traceSession)
            }
        }
    }

    override fun trace(element: EbicsRootElement, traceSession: org.ebics.client.api.trace.h005.ITraceSession) {
        if (traceEnabled) {
            with(traceSession) {
                val ebicsServiceType = orderType.service?.let { service -> EbicsService.fromEbicsService(service) }
                val orderType =
                    OrderTypeDefinition(orderType.adminOrderType, ebicsServiceType = ebicsServiceType)
                trace(element, orderType, traceSession)
            }
        }
    }

    private fun trace(element: EbicsRootElement, orderType: OrderTypeDefinition, traceSession: ITraceSession) {
        with(traceSession) {
            val user = session.user as BankConnectionEntity
            traceRepository.save(
                TraceEntry(
                    null, element.toString(), user, session.sessionId, orderNumber, ebicsVersion, upload,
                    orderType = orderType, traceType = TraceType.EbicsEnvelope
                )
            )
        }
    }

    override fun setTraceEnabled(enabled: Boolean) {
        traceEnabled = enabled
    }

    fun findTraces(): List<TraceEntry> {
        return traceRepository.findAll().filter { it.hasReadAccess(AuthenticationContext.fromSecurityContext()) }
    }
}