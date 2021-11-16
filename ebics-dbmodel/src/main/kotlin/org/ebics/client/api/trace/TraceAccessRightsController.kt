package org.ebics.client.api.trace

import org.ebics.client.api.security.*
import org.slf4j.LoggerFactory

/**
 * This interface is checking access to bank connection for the logged user
 */
interface TraceAccessRightsController : ReadAccessRightsController {

    /**
     * Indicates whom belongs the trace entry (userid)
     */
    fun getOwnerName(): String

    /**
     * Return true if the actual security context of the request is authorized for the reading of BankConnectionPermissionInfo attributes
     */
    override fun hasReadAccess(authCtx: AuthenticationContext): Boolean {
        with(authCtx) {
            return when {
                authCtx.hasRole(BusinessRole.ROLE_ADMIN) -> {
                    logger.debug("Read permission granted through admin role for {}", authCtx.name)
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_USER) && getOwnerName() == name -> {
                    logger.debug("Read permission granted through user access for {}", authCtx.name)
                    true
                }
                else -> {
                    logger.debug(
                        "Read permission denied, no role available '{}' for '{}'",
                        authorities.joinToString(),
                        authCtx.name
                    )
                    false
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TraceAccessRightsController::class.java)
    }
}