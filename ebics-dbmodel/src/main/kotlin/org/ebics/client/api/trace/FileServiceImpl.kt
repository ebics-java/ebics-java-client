package org.ebics.client.api.trace

import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.user.User
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class FileServiceImpl(private val traceRepository: TraceRepository) : FileService {
    override fun getLastDownloadedFile(
        orderType: OrderTypeDefinition,
        user: User,
        useSharedPartnerData: Boolean
    ): TraceEntry {
        val authCtx = AuthenticationContext.fromSecurityContext()
        return traceRepository
            .findAll(
                creatorAndOtAndBcEquals(authCtx.name, orderType, user, useSharedPartnerData),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "dateTime"))
            )
            .single { it.hasReadAccess(authCtx) }
    }

    override fun addTextFile(user: User, orderType: OrderTypeDefinition, fileContent: String) {
        traceRepository.save(TraceEntry(null, fileContent, user, orderType = orderType))
    }
}