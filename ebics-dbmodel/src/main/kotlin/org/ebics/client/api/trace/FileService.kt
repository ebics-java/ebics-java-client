package org.ebics.client.api.trace

import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
open class FileService(private val traceRepository: TraceRepository,
                  @Value("\${housekeeping.trace.older-than-days:30}")
                  private val houseKeepOlderThanDays: Long) : IFileService {

    override fun getLastDownloadedFile(
        orderType: OrderTypeDefinition,
        user: BankConnectionEntity,
        ebicsVersion: EbicsVersion,
        useSharedPartnerData: Boolean
    ): TraceEntry {
        val authCtx = AuthenticationContext.fromSecurityContext()
        return traceRepository
            .findAll(
                fileDownloadFilter(authCtx.name, orderType, user, ebicsVersion, useSharedPartnerData),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "dateTime"))
            )
            .single { it.hasReadAccess(authCtx) }.also {
                logger.debug("File retrieved from file service, orderType {}, bank connection id={}, userId={}, ebicsVersion={}", orderType.toString(), user.id, user.userId, ebicsVersion)
            }
    }

    override fun addTextFile(
        user: BankConnectionEntity,
        orderType: OrderTypeDefinition,
        fileContent: String,
        sessionId: String,
        orderNumber: String?,
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

    //Transactional here should solve error: No EntityManager with actual transaction available for current thread
    @Transactional
    override fun removeAllFilesOlderThan(@Value("$\\{value.from.file\\}") dateTime: ZonedDateTime) {
        val numberOfRemovedEntries = traceRepository.deleteByDateTimeLessThan(dateTime)
        logger.info("Total '{}' TraceEntries removed", numberOfRemovedEntries)
    }

    @Scheduled(cron = "0 0 1 * * *")
    fun houseKeeping() {
        logger.info("House keeping of TraceEntries older than {} days", houseKeepOlderThanDays)
        removeAllFilesOlderThan(ZonedDateTime.now().minusDays(houseKeepOlderThanDays))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileService::class.java)
    }
}