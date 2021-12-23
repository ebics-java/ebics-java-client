package org.ebics.client.api.trace.h004

import org.ebics.client.api.trace.ITraceSession
import org.ebics.client.order.IOrderTypeDefinition25

interface ITraceSession : ITraceSession {
    override val orderType: IOrderTypeDefinition25
}