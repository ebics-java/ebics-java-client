package org.ebics.client.api.trace

import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.user.User
import org.ebics.client.model.EbicsVersion
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class FileServiceImpl(private val traceRepository: TraceRepository) : FileService {
    override fun getLastDownloadedFile(
        orderType: OrderTypeDefinition,
        user: User,
        ebicsVersion: EbicsVersion,
        useSharedPartnerData: Boolean
    ): TraceEntry {
        val authCtx = AuthenticationContext.fromSecurityContext()
        return traceRepository
            .findAll(
                fileDownloadFilter(authCtx.name, orderType, user, ebicsVersion, useSharedPartnerData),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "dateTime"))
            )
            .single { it.hasReadAccess(authCtx) }
    }

    override fun addTextFile(
        user: User,
        orderType: OrderTypeDefinition,
        fileContent: String,
        sessionId: String,
        orderNumber: String,
        ebicsVersion: EbicsVersion,
        upload: Boolean
    ) {
        traceRepository.save(
            TraceEntry(
                null,
                fileContent,
                user,
                sessionId,
                orderNumber,
                ebicsVersion,
                upload,
                orderType = orderType,
                traceType = TraceType.Content
            )
        )
    }
}