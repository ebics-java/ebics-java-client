package org.ebics.client.api.trace

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
data class TraceEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    @Lob
    val messageBody:String,

    @ManyToOne(optional = false)
    val user:BankConnectionEntity,

    val sessionId: String,

    val orderNumber: String? = null,
    /**
     * The EBICS version used for this entry
     */
    val ebicsVersion: EbicsVersion,

    val upload: Boolean,

    /**
     * Web user who created this entry
     */
    val creator: String = AuthenticationContext.fromSecurityContext().name,

    /**
     * Time when was the entry created
     */
    val dateTime: ZonedDateTime = ZonedDateTime.now(),

    /**
     * Optional Order Type of this entry
     *  (currently null by standard EBICS tracing)
     */
    @Embedded
    val orderType: OrderTypeDefinition? = null,

    val traceType: TraceType = TraceType.EbicsEnvelope
) : TraceAccessRightsController {
    @JsonIgnore
    override fun getObjectName(): String = "Trace entry created by '$creator' from $dateTime"

    @JsonIgnore
    override fun getOwnerName(): String = creator
}
