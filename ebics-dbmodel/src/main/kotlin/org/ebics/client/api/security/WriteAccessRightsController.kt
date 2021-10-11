package org.ebics.client.api.security

interface WriteAccessRightsController : NamedObject {
    fun hasWriteAccess(authCtx: AuthenticationContext = AuthenticationContext.fromSecurityContext()): Boolean
    fun checkWriteAccess(authCtx: AuthenticationContext = AuthenticationContext.fromSecurityContext()) {
        if (!hasWriteAccess(authCtx))
            throw IllegalAccessException("Web user '${authCtx.name}' is not authorized for changing of object '${getObjectName()}'")
    }
}