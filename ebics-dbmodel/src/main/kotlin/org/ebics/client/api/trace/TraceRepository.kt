package org.ebics.client.api.trace

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface TraceRepository : JpaRepository<TraceEntry, Long>, JpaSpecificationExecutor<TraceEntry>