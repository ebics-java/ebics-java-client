package org.ebics.client.api.security

interface UseAccessRightsController : NamedObject {
    fun hasUseAccess(authCtx: AuthenticationContext = AuthenticationContext.fromSecurityContext()): Boolean
    fun checkUseAccess(authCtx: AuthenticationContext = AuthenticationContext.fromSecurityContext()) {
        if (!hasUseAccess(authCtx))
            throw IllegalAccessException("Web user '${authCtx.name}' is not authorized for USE of object '${getObjectName()}'")
    }
}