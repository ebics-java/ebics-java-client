package org.ebics.client.ebicsrestapi.trace

import org.ebics.client.api.trace.TraceEntry
import org.ebics.client.api.trace.TraceService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("traces")
@CrossOrigin(origins = ["http://localhost:8081"])
class TraceResource(val traceService: TraceService) {
    @GetMapping()
    fun listTraces(): List<TraceEntry> = traceService.findTraces()
}