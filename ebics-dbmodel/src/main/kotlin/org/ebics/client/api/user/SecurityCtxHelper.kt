package org.ebics.client.api.user

import org.ebics.client.api.user.settings.UserSettings
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

class SecurityCtxHelper {
    companion object {
        fun isGuestRole(roleName: String) = roleName == "ROLE_GUEST"
        fun isUserRole(roleName: String) = roleName == "ROLE_USER"
        fun isAdminRole(roleName: String) = roleName == "ROLE_ADMIN"

        /**
         * Helper method to get current user authentication context
         * Authentication can be null (usually if the spring security is wrongly configured)
         * This would otherwise throw null pointer exception, here overridden to IllegalAccessException
         */
        fun getAuthentication(): Authentication {
            if (SecurityContextHolder.getContext() != null) {
                if (SecurityContextHolder.getContext().authentication != null)
                    return SecurityContextHolder.getContext().authentication
                else
                    throw IllegalAccessException("Security context doesn't have authentication object, please check the spring security configuration");
            } else
                throw IllegalAccessException("Security context holder doesn't have context object, please check the spring security configuration");
        }

        fun getPrincipalName(): String = getAuthentication().name

        /**
         * Return true if the actual security context of the request is authorized for the reading of user attributes
         */
        fun isAuthorizedForUserRead(
            user: User,
            authentication: Authentication = getAuthentication()
        ): Boolean {
            with(authentication) {
                return when {
                    authorities.any { isAdminRole(it.authority) } -> true
                    authorities.any { isUserRole(it.authority) } -> user.guestAccess || user.creator == name
                    else -> user.guestAccess
                }
            }
        }

        /**
         * Return true if the actual security context of the request is authorized for the changing/adding/deleting of user attributes
         */
        private fun isAuthorizedForUserWrite(
            user: User,
            authentication: Authentication = getAuthentication()
        ): Boolean {
            with(authentication) {
                return when {
                    authorities.any { isAdminRole(it.authority) } -> true
                    authorities.any { isUserRole(it.authority) } -> user.creator == name
                    else -> false
                }
            }
        }

        fun checkWriteAuthorization(
            user: User,
            authentication: Authentication = getAuthentication()
        ) {
            if (!isAuthorizedForUserWrite(user, authentication))
                throw IllegalAccessException("Web user '${authentication.name}' is not authorized for changing of EBICS bank connection: '${user.name}'")
        }

        fun checkWriteAuthorization(
            userSettings: UserSettings,
            userId: String = getPrincipalName()
        ) {
            if (userSettings.userId != userId)
                throw IllegalAccessException("Web user '${userId}' is not authorized for changing of user settings for: '${userSettings.userId}'")
        }
    }
}