package org.ebics.client.certificate

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*

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
                aliasPrefix to EbicsUserCertificates(
                    manager.getCertificate("$aliasPrefix-A005"),
                    manager.getCertificate("$aliasPrefix-X002"),
                    manager.getCertificate("$aliasPrefix-E002"),
                    manager.getPrivateKey("$aliasPrefix-A005"),
                    manager.getPrivateKey("$aliasPrefix-X002"),
                    manager.getPrivateKey("$aliasPrefix-E002")
                )
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
        with(KeyStoreManager.create(password)) {
            certs.forEach { keyStoreEntry ->
                val keyStoreAliasPrefix = keyStoreEntry.key
                val keyStoreSingleCertEntry = keyStoreEntry.value
                with(keyStoreSingleCertEntry) {
                    setKeyEntry("$keyStoreAliasPrefix-A005", a005PrivateKey, a005Certificate)
                    setKeyEntry("$keyStoreAliasPrefix-X002", x002PrivateKey, x002Certificate)
                    setKeyEntry("$keyStoreAliasPrefix-E002", e002PrivateKey, e002Certificate)
                }
            }
            save(os)
        }
    }
}