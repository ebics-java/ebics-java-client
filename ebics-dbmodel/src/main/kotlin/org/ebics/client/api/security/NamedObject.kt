package org.ebics.client.api.security

interface NamedObject {
    /**
     * Returns business description of object which access rights are checked (for logging purposes)
     */
    fun getObjectName(): String
}