package org.ebics.client.api.trace.h005

import org.ebics.client.api.trace.ITraceSession
import org.ebics.client.order.IOrderTypeDefinition30

interface ITraceSession : ITraceSession {
    override val orderType: IOrderTypeDefinition30
}