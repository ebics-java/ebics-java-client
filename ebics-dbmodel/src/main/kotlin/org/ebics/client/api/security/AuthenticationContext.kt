package org.ebics.client.api.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder.getContext

/**
 * This class is used to abstract from spring security context,
 * in a way that authorities are mapped to business roles
 */
class AuthenticationContext(authentication: Authentication) : Authentication by authentication {

    /**
     * The spring security context authorities are mapped to application business roles
     *  further are only roles used, no direct access to authorities
     */
    val roles:Set<BusinessRole> = authorities.map { BusinessRole.fromString(it.authority) }.toSet()
    fun hasRole(role: BusinessRole): Boolean = roles.contains(role)

    companion object {
        /**
         * Save getting of spring security context from a holder
         */
        fun fromSecurityContext(): AuthenticationContext {
            if (getContext() != null) {
                if (getContext().authentication != null)
                    return AuthenticationContext(getContext().authentication)
                else
                    throw IllegalAccessException("Security context doesn't have authentication object, please check the spring security configuration")
            } else
                throw IllegalAccessException("Security context holder doesn't have context object, please check the spring security configuration")
        }
    }
}

