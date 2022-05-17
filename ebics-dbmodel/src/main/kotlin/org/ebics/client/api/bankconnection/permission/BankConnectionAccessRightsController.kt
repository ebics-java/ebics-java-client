package org.ebics.client.api.bankconnection.permission

import org.ebics.client.api.security.*
import org.slf4j.LoggerFactory

/**
 * This interface is checking access to bank connection for the logged user
 */
interface BankConnectionAccessRightsController : WriteAccessRightsController, ReadAccessRightsController, UseAccessRightsController {

    /**
     * Indicates who created the bank connection (userid)
     */
    fun getCreatorName(): String

    /**
     * Indicates shared bank connection
     */
    fun isGuestAccess(): Boolean

    fun hasAccess(permission: BankConnectionAccessType, authCtx: AuthenticationContext = AuthenticationContext.fromSecurityContext()): Boolean {
        return when (permission) {
            BankConnectionAccessType.READ -> hasReadAccess(authCtx)
            BankConnectionAccessType.WRITE -> hasWriteAccess(authCtx)
            BankConnectionAccessType.USE -> hasUseAccess(authCtx)
        }
    }

    fun checkAccess(permission: BankConnectionAccessType, authCtx: AuthenticationContext = AuthenticationContext.fromSecurityContext()) {
        when (permission) {
            BankConnectionAccessType.READ -> checkReadAccess(authCtx)
            BankConnectionAccessType.WRITE -> checkWriteAccess(authCtx)
            BankConnectionAccessType.USE -> checkUseAccess(authCtx)
        }
    }

    /**
     * Return true if the actual security context of the request is authorized for the changing/adding/deleting of BankConnectionPermissionInfo attributes
     */
    override fun hasWriteAccess(authCtx: AuthenticationContext): Boolean {
        with(authCtx) {
            return when {
                authCtx.hasRole(BusinessRole.ROLE_ADMIN) -> {
                    logger.debug("Write permission for '{}' granted through admin role for '{}'", getObjectName(), authCtx.name)
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_USER) && getCreatorName() == name -> {
                    logger.debug("Write permission for '{}' granted through user access for '{}'", getObjectName(), authCtx.name)
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
                    logger.debug("Read permission for '{}' granted through admin role for '{}'", getObjectName(), authCtx.name)
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_USER) && getCreatorName() == name -> {
                    logger.debug("Read permission for '{}' granted through user access for '{}'", getObjectName(), authCtx.name)
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_GUEST) && isGuestAccess() -> {
                    logger.debug("Read permission for '{}' granted through guest access for '{}'", getObjectName(), authCtx.name)
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

    /**
     * Return true if the actual security context of the request is authorized for the using of bank connection (Upload/Download)
     */
    override fun hasUseAccess(authCtx: AuthenticationContext): Boolean {
        with(authCtx) {
            return when {
                authCtx.hasRole(BusinessRole.ROLE_USER) && getCreatorName() == name -> {
                    logger.debug("Use permission for '{}' granted through user access for '{}'", getObjectName(), authCtx.name)
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_GUEST) && isGuestAccess() -> {
                    logger.debug("Use permission for '{}' granted through guest access for '{}'", getObjectName(), authCtx.name)
                    true
                }
                else -> {
                    logger.debug(
                        "Use permission for '{}' denied, no role available '{}' for '{}'",
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
        private val logger = LoggerFactory.getLogger(BankConnectionAccessRightsController::class.java)
    }
}