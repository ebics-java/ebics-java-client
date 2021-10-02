package org.ebics.client.api.user

import org.ebics.client.api.BankConnectionPermission
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

        fun isAuthorizedFor(bankConnection: User, permission: BankConnectionPermission, authentication: Authentication = getAuthentication()): Boolean {
            return when (permission) {
                BankConnectionPermission.READ -> isAuthorizedForBankConnectionRead(bankConnection, authentication)
                BankConnectionPermission.WRITE -> isAuthorizedForBankConnectionWrite(bankConnection, authentication)
                BankConnectionPermission.USE -> isAuthorizedForBankConnectionUse(bankConnection, authentication)
            }
        }

        /**
         * Return true if the actual security context of the request is authorized for the reading of user attributes
         */
        fun isAuthorizedForBankConnectionRead(
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
         * Return true if the actual security context of the request is authorized for the using of bank connection (Upload/Download)
         */
        fun isAuthorizedForBankConnectionUse(
            user: User,
            authentication: Authentication = getAuthentication()
        ): Boolean {
            with(authentication) {
                return user.guestAccess || user.creator == name
            }
        }

        /**
         * Return true if the actual security context of the request is authorized for the changing/adding/deleting of user attributes
         */
        private fun isAuthorizedForBankConnectionWrite(
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

        fun checkAuthorization(bankConnection: User, permission: BankConnectionPermission, authentication: Authentication = getAuthentication()) {
            when (permission) {
                BankConnectionPermission.READ -> checkReadAuthorization(bankConnection, authentication)
                BankConnectionPermission.WRITE -> checkWriteAuthorization(bankConnection, authentication)
                BankConnectionPermission.USE -> checkUseAuthorization(bankConnection, authentication)
            }
        }

        fun checkReadAuthorization(
            user: User,
            authentication: Authentication = getAuthentication()
        ) {
            if (!isAuthorizedForBankConnectionRead(user, authentication))
                throw IllegalAccessException("Web user '${authentication.name}' is not authorized for reading of EBICS bank connection: '${user.name}'")
        }

        fun checkWriteAuthorization(
            user: User,
            authentication: Authentication = getAuthentication()
        ) {
            if (!isAuthorizedForBankConnectionWrite(user, authentication))
                throw IllegalAccessException("Web user '${authentication.name}' is not authorized for changing of EBICS bank connection: '${user.name}'")
        }

        fun checkUseAuthorization(user: User, authentication: Authentication = getAuthentication()) {
            if (!isAuthorizedForBankConnectionUse(user, authentication))
                throw IllegalAccessException("Web user '${authentication.name}' is not authorized for using of EBICS bank connection: '${user.name}'")
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