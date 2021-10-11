package org.ebics.client.api.security

import org.springframework.security.core.GrantedAuthority

enum class BusinessRole {
    ROLE_ADMIN,
    ROLE_USER,
    ROLE_GUEST,
    ROLE_UNKNOWN;

    companion object {
        fun fromString(roleName: String): BusinessRole {
            return when (roleName) {
                "ROLE_ADMIN" -> ROLE_ADMIN
                "ROLE_USER" -> ROLE_USER
                "ROLE_GUEST" -> ROLE_GUEST
                else -> ROLE_UNKNOWN
            }
        }
    }
}