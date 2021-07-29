package org.ebics.client.ebicsrestapi

import org.ebics.client.api.Configuration
import org.ebics.client.api.LetterManager
import org.ebics.client.api.TraceManager
import org.ebics.client.api.trace.TraceService
import org.ebics.client.messages.Messages
import org.springframework.beans.factory.annotation.Value
import java.util.*

@org.springframework.context.annotation.Configuration
class EbicsRestConfiguration(
    @Value("\${http.proxy.host:#{null}}")
    override val httpProxyHost: String?,

    @Value("\${http.proxy.port:#{null}}")
    override val httpProxyPort: Int?,

    @Value("\${http.proxy.user:#{null}}")
    override val httpProxyUser: String?,

    @Value("\${http.proxy.password:#{null}}")
    override val httpProxyPassword: String?,

    @Value("\${ssl.truststore:#{null}}")
    override val sslTrustedStoreFile: String?,

    @Value("\${ebics.signatureVersion:#{A005}}")
    override val signatureVersion: String,

    @Value("\${ebics.authenticationVersion:#{X002}}")
    override val authenticationVersion: String,

    @Value("\${ebics.encryptionVersion:#{E002}}")
    override val encryptionVersion: String,

    @Value("\${ebics.trace:#{true}}")
    override val isTraceEnabled: Boolean,

    @Value("\${ebics.compression:#{true}}")
    override val isCompressionEnabled: Boolean,

    @Value("\${ebics.locale.language:#{en}}")
    private val localeLanguage:String,

    traceService: TraceService
) : Configuration {

    override val traceManager: TraceManager = traceService
    override val letterManager: LetterManager
        get() = TODO("Not yet implemented")


    final override val locale: Locale = Locale(localeLanguage)

    init {
        //Setting default locale as well in order to set locale for Messages singleton object
        Locale.setDefault(locale)
    }
}