package org.ebics.client.api.bankconnection

import java.util.*

class DomainNameGenerator(userName: String, country: String?) {
    val certificateDnCn = DomainNameComponent("cn", userName.sanitizeForDn())
    val certificateDnCountry = DomainNameComponent("c", country?.sanitizeForDn())

    override fun toString(): String {
        return createDomainName(
            listOf(
                certificateDnCn, certificateDnCountry
            )
        )
    }

    private fun String.sanitizeForDn():String = lowercase(Locale.getDefault()).removeNonAZazDigits().replaceSpaceByMinus()
    private fun String.removeNonAZazDigits(): String = replace("[^A-Za-z0-9\\s]".toRegex(), "")
    private fun String.replaceSpaceByMinus(): String = replace("\\s".toRegex(), "-")

    private fun createDomainName(dnComponents: List<DomainNameComponent>): String {
        return dnComponents.filter { !it.value.isNullOrBlank() }.joinToString(",") { "${it.name}=${it.value}" }
    }

    class DomainNameComponent(val name: String, val value: String?)
}

