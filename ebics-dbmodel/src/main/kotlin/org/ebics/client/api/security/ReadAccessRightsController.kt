package org.ebics.client.api.security

interface ReadAccessRightsController : NamedObject {
    fun hasReadAccess(authCtx: AuthenticationContext = AuthenticationContext.fromSecurityContext()): Boolean
    fun checkReadAccess(authCtx: AuthenticationContext = AuthenticationContext.fromSecurityContext()) {
        if (!hasReadAccess(authCtx))
            throw IllegalAccessException("Web user '${authCtx.name}' is not authorized for READing of object '${getObjectName()}'")
    }
}