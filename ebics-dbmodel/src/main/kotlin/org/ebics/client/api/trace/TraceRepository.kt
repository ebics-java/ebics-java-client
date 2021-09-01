package org.ebics.client.api.trace

import org.ebics.client.api.CustomJpaRepository

interface TraceRepository : CustomJpaRepository<TraceEntry, Long>