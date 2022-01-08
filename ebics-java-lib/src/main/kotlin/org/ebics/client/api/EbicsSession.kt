package org.ebics.client.api

import org.ebics.client.certificate.BankCertificateManager
import org.ebics.client.certificate.UserCertificateManager
import org.ebics.client.exception.EbicsException
import org.ebics.client.model.Product

interface EbicsSession {
    val sessionId: String
    /**
     * The session user.
     */
    val user: EbicsUser
    /**
     * The client application configuration.
     */
    val configuration: EbicsConfiguration
    /**
     * Sets the optional product identification that will be sent to the bank during each request.
     */
    val product: Product

    /**
     * User key-pairs (A005, X002, E002)
     */
    val userCert: UserCertificateManager

    /**
     * Bank public keys (X002, E002)
     */
    val bankCert: BankCertificateManager?

    /**
     * Adds a session parameter to use it in the transfer process.
     * @param key the parameter key
     * @param value the parameter value
     */
    fun addSessionParam(key: String, value: String) {
        parameters[key] = value
    }

    /**
     * Retrieves a session parameter using its key.
     * @param key the parameter key
     * @return the session parameter
     */
    fun getSessionParam(key: String): String? = parameters[key]

    val parameters: MutableMap<String, String>

    /**
     * Returns the bank id.
     * @return the bank id.
     * @throws EbicsException
     */
    @Throws(EbicsException::class)
    fun getBankID(): String {
        return user.partner.bank.hostId
    }
}