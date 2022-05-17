package org.ebics.client.api.trace

import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.metamodel.SingularAttribute

/**
 * Add equals predicate to existing expressions
 * If the given value to be searched is not null
 */
fun <T, X> Predicate.addEqualsIfNotNull(builder: CriteriaBuilder, path: Path<X>, attributeName: String, value: T?) {
    if (value != null) {
        expressions.add(
            builder.attributeEquals(path, attributeName, value)
        )
    }
}

fun <T, X> CriteriaBuilder.attributeEquals(path: Path<X>, attributeName: String, value: T): Predicate {
    return equal(path.get<SingularAttribute<X, T>>(attributeName), value)
}

fun bankConnectionEquals(user: BankConnectionEntity, useSharedPartnerData: Boolean = true): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        val p = builder.disjunction()
        val userAttr = root.get<SingularAttribute<TraceEntry, String>>("user")
        p.addEqualsIfNotNull(builder, userAttr, "id", user.id)
        if (useSharedPartnerData) {
            val partnerAttr = userAttr.get<SingularAttribute<TraceEntry, String>>("partner")
            p.addEqualsIfNotNull(builder, partnerAttr, "id", user.partner.id)
        }
        p
    }
}

fun creatorEquals(creator: String): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "creator", creator)
    }
}

fun orderTypeEquals(orderType: OrderTypeDefinition): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        val p = builder.conjunction()
        with(orderType) {
            val orderTypeAttr = root.get<SingularAttribute<TraceEntry, String>>("orderType")
            p.addEqualsIfNotNull(builder, orderTypeAttr, "adminOrderType", adminOrderType)
            p.addEqualsIfNotNull(builder, orderTypeAttr, "businessOrderType", businessOrderType)
            if (ebicsServiceType != null) {
                with(ebicsServiceType) {
                    val serviceTypeAttr = orderTypeAttr.get<SingularAttribute<TraceEntry, String>>("ebicsServiceType")
                    p.addEqualsIfNotNull(builder, serviceTypeAttr, "serviceName", serviceName)
                    p.addEqualsIfNotNull(builder, serviceTypeAttr, "serviceOption", serviceOption)
                    p.addEqualsIfNotNull(builder, serviceTypeAttr, "scope", scope)
                    p.addEqualsIfNotNull(builder, serviceTypeAttr, "containerType", containerType)
                    with(message) {
                        val messageAttr = serviceTypeAttr.get<SingularAttribute<TraceEntry, String>>("message")
                        p.addEqualsIfNotNull(builder, messageAttr, "messageName", messageName)
                        p.addEqualsIfNotNull(builder, messageAttr, "messageNameFormat", messageNameFormat)
                        p.addEqualsIfNotNull(builder, messageAttr, "messageNameVariant", messageNameVariant)
                        p.addEqualsIfNotNull(builder, messageAttr, "messageNameVersion", messageNameVersion)
                    }
                }
            }
        }
        p
    }
}

fun sessionIdEquals(sessionId: String): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "sessionId", sessionId)
    }
}

fun ebicsVersionEquals(ebicsVersion: EbicsVersion): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "ebicsVersion", ebicsVersion)
    }
}

fun uploadEquals(upload: Boolean): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "upload", upload)
    }
}

fun traceTypeEquals(traceType: TraceType): Specification<TraceEntry> {
    return Specification<TraceEntry> { root, _, builder ->
        builder.attributeEquals(root, "traceType", traceType)
    }
}

fun fileDownloadFilter(creator: String, orderType: OrderTypeDefinition, user: BankConnectionEntity, ebicsVersion: EbicsVersion, useSharedPartnerData: Boolean = true): Specification<TraceEntry> {
    return creatorEquals(creator)
        .and(orderTypeEquals(orderType))
        .and(bankConnectionEquals(user, useSharedPartnerData))
        .and(ebicsVersionEquals(ebicsVersion))
        .and(uploadEquals(false))
        .and(traceTypeEquals(TraceType.Content))
}