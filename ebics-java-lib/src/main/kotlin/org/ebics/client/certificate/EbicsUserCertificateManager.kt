package org.ebics.client.certificate

import java.io.InputStream
import java.io.OutputStream

class EbicsUserCertificateManager(private val certs: MutableMap<String, EbicsUserCertificates>) :
    Map<String, EbicsUserCertificates> by certs {

    companion object {
        fun createEmpty(): EbicsUserCertificateManager =
            EbicsUserCertificateManager(mutableMapOf())

        fun load(ins: InputStream, password: String): EbicsUserCertificateManager {
            val manager = KeyStoreManager.load(ins, password)
            val aliasPrefixes: Set<String> =
                manager.aliases().toList()
                    .map { it.replace("-((A005)|(E002)|(X002))$".toRegex(), "") }
                    .toSet()
            val singleCertEntries = aliasPrefixes.map { aliasPrefix ->
                aliasPrefix to EbicsUserCertificates.load(manager, aliasPrefix)
            }
            return EbicsUserCertificateManager(singleCertEntries.toMap().toMutableMap())
        }
    }

    fun add(userDn: String, aliasPrefix: String = userDn) {
        require(!certs.containsKey(aliasPrefix)) { "The alias $aliasPrefix exists already" }
        certs[aliasPrefix] = EbicsUserCertificates.create(userDn)
    }

    fun remove(aliasPrefix: String) {
        require(certs.containsKey(aliasPrefix)) { "The alias $aliasPrefix doesn't exist" }
        certs.remove(aliasPrefix)
    }

    fun save(os: OutputStream, password: String) {
        val manager = KeyStoreManager.create(password)
        certs.forEach { ebicsUserCerts ->
            val aliasPrefix = ebicsUserCerts.key
            val userCerts = ebicsUserCerts.value
            userCerts.save(manager, aliasPrefix)
        }
        manager.save(os)
    }
}