package org.ebics.client.api.bankconnection.settings

import org.ebics.client.api.security.*
import org.slf4j.LoggerFactory

/**
 * This interface is checking access to bank connection for the logged user
 */
interface UserSettingsAccessRightsController : WriteAccessRightsController, ReadAccessRightsController {

    /**
     * Indicates whom belongs the bank connection (userid)
     */
    fun getOwnerName(): String

    /**
     * Return true if the actual security context of the request is authorized for the changing/adding/deleting of BankConnectionPermissionInfo attributes
     */
    override fun hasWriteAccess(authCtx: AuthenticationContext): Boolean {
        with(authCtx) {
            return when {
                authCtx.hasRole(BusinessRole.ROLE_ADMIN) -> {
                    logger.debug(
                        "Write permission for '{}' granted through admin role for '{}'",
                        getObjectName(),
                        authCtx.name
                    )
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_USER) && getOwnerName() == name -> {
                    logger.debug(
                        "Write permission for '{}' granted through user access for '{}'",
                        getObjectName(),
                        authCtx.name
                    )
                    true
                }
                else -> {
                    logger.debug(
                        "Write permission for '{}' denied, no role available '{}' for '{}'",
                        getObjectName(),
                        authorities.joinToString(),
                        authCtx.name
                    )
                    false
                }
            }
        }
    }

    /**
     * Return true if the actual security context of the request is authorized for the reading of BankConnectionPermissionInfo attributes
     */
    override fun hasReadAccess(authCtx: AuthenticationContext): Boolean {
        with(authCtx) {
            return when {
                authCtx.hasRole(BusinessRole.ROLE_ADMIN) -> {
                    logger.debug(
                        "Read permission for '{}' granted through admin role for '{}'",
                        getObjectName(),
                        authCtx.name
                    )
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_USER) && getOwnerName() == name -> {
                    logger.debug(
                        "Read permission for '{}' granted through user access for '{}'",
                        getObjectName(),
                        authCtx.name
                    )
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_GUEST) -> {
                    logger.debug(
                        "Read permission for '{}' granted through guest access for '{}'",
                        getObjectName(),
                        authCtx.name
                    )
                    true
                }
                else -> {
                    logger.debug(
                        "Read permission for '{}' denied, no role available '{}' for '{}'",
                        getObjectName(),
                        authorities.joinToString(),
                        authCtx.name
                    )
                    false
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserSettingsAccessRightsController::class.java)
    }
}