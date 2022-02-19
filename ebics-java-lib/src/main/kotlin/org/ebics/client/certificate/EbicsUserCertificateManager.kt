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
    private enum class KeyType { A005, X002, E002, }
    private class CertKeyPair(val certificate: X509Certificate, val privateKey: PrivateKey)

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

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun create(userDn: String): EbicsUserCertificates {
        try {
            val calendar: Calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, X509Constants.DEFAULT_DURATION)
            val endDate = Date(calendar.timeInMillis)
            val a005pair = createCertificate(KeyType.A005, userDn, endDate)
            val x002pair = createCertificate(KeyType.X002, userDn, endDate)
            val e002pair = createCertificate(KeyType.E002, userDn, endDate)
            return EbicsUserCertificates(
                a005pair.certificate,
                x002pair.certificate,
                e002pair.certificate,
                a005pair.privateKey,
                x002pair.privateKey,
                e002pair.privateKey,
            )
        } catch (ex: Exception) {
            throw IllegalArgumentException("Cant create certificate for dn='$userDn' error: ${ex.message}", ex)
        }
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun createCertificate(
        keyType: KeyType,
        userDn: String,
        end: Date
    ): CertKeyPair {
        val keypair: KeyPair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE)
        val cert = when (keyType) {
            KeyType.A005 -> X509Generator.generateA005Certificate(keypair, userDn, Date(), end)
            KeyType.X002 -> X509Generator.generateX002Certificate(keypair, userDn, Date(), end)
            KeyType.E002 -> X509Generator.generateE002Certificate(keypair, userDn, Date(), end)
        }
        return CertKeyPair(cert, keypair.private)
    }

    fun add(userDn: String, aliasPrefix: String = userDn) {
        require(!certs.containsKey(aliasPrefix)) { "The alias $aliasPrefix exists already" }
        certs[aliasPrefix] = create(userDn)
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