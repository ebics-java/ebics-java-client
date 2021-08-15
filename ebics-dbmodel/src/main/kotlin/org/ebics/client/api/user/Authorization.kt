package org.ebics.client.api.user

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

class Authorization {
    companion object {
        /**
         * Return true if the actual security context of the request is authorized for the reading of user attributes
         */
        fun isAuthorizedForUserRead(
            user: User,
            authentication: Authentication = SecurityContextHolder.getContext().authentication
        ): Boolean {
            with(authentication) {
                return when {
                    authorities.any{it.authority == "ROLE_ADMIN"} -> true
                    authorities.any{it.authority == "ROLE_USER"} -> user.guestAccess || user.creator == name
                    else -> user.guestAccess
                }
            }
        }

        /**
         * Return true if the actual security context of the request is authorized for the changing/adding/deleting of user attributes
         */
        private fun isAuthorizedForUserWrite(
            user: User,
            authentication: Authentication = SecurityContextHolder.getContext().authentication
        ): Boolean {
            with(authentication) {
                return when {
                    authorities.any{it.authority == "ROLE_ADMIN"} -> true
                    authorities.any{it.authority == "ROLE_USER"} -> user.creator == name
                    else -> false
                }
            }
        }

        fun checkWriteAuthorization(
            user: User,
            authentication: Authentication = SecurityContextHolder.getContext().authentication
        ) {
            if (!isAuthorizedForUserWrite(user, authentication))
                throw IllegalAccessException("Web user '${authentication.name}' is not authorized for changing of EBICS user: '${user.name}'")
        }
    }
}