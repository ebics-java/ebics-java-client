package org.ebics.client.api.trace

import org.springframework.data.jpa.repository.JpaRepository

interface TraceRepository : JpaRepository<TraceEntry, Long>