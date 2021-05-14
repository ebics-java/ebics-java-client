package org.ebics.client.console

import org.ebics.client.user.EbicsModel
import org.ebics.client.user.EbicsVersion
import org.ebics.client.user.serializable.User
import org.ebics.client.interfaces.Configuration
import org.ebics.client.interfaces.PasswordCallback
import org.ebics.client.session.DefaultConfiguration
import org.ebics.client.user.Product
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.util.*

/**
 * Ebics client app
 * This is the base part of Console App without being depended on specific EBICS h00X API.
 */
class ConsoleAppBase constructor(configuration: Configuration,
                                 private val properties: ConfigProperties,
                                 val defaultProduct: Product
) {
    val ebicsModel: EbicsModel =
        EbicsModel(configuration)

    @Throws(Exception::class)
    private fun createUser(properties: ConfigProperties, pwdHandler: PasswordCallback, ebicsVersion: EbicsVersion): User {
        val userId = properties["userId"]
        val partnerId = properties["partnerId"]
        val bankUrl = properties["bank.url"]
        val bankName = properties["bank.name"]
        val hostId = properties["hostId"]
        val userName = properties["user.name"]
        val userEmail = properties["user.email"]
        val userCountry = properties["user.country"]
        val userOrg = properties["user.org"]
        //Due to missing h005.PubKeyInfoType.getPubKeyValue must be certificates used in EBICS 3.0
        val useCertificates: Boolean = ebicsVersion == EbicsVersion.H005
        return ebicsModel.createUser(URL(bankUrl), ebicsVersion, bankName, hostId, partnerId, userId, userName, userEmail,
                userCountry, userOrg, useCertificates, true, pwdHandler)
    }

    @Throws(Exception::class)
    fun createDefaultUser(ebicsVersion: EbicsVersion): User = createUser(properties, createPasswordCallback(), ebicsVersion)

    @Throws(Exception::class)
    fun loadDefaultUser(): User {
        val userId = properties["userId"]
        val hostId = properties["hostId"]
        val partnerId = properties["partnerId"]
        return ebicsModel.loadUser(hostId, partnerId, userId)
    }

    fun createPasswordCallback(): PasswordCallback {
        val password = properties["password"]
        return PasswordCallback { password.toCharArray() }
    }

    companion object {
        const val CONSOLE_APP_BUNDLE_NAME = "org.ebics.client.console.messages"
        @JvmStatic
        @Throws(FileNotFoundException::class, IOException::class)
        fun createConsoleApp(rootDir: File, defaultEbicsConfigFile: File): ConsoleAppBase {
            val properties = ConfigProperties(defaultEbicsConfigFile)
            val country = properties["countryCode"].toUpperCase()
            val language = properties["languageCode"].toLowerCase()
            val productName = properties["productName"]
            val locale = Locale(language, country)
            val configuration: DefaultConfiguration = object : DefaultConfiguration(rootDir.absolutePath) {
                override fun getLocale(): Locale {
                    return locale
                }
            }
            val product = Product(productName, language, null)
            return ConsoleAppBase(configuration, properties, product)
        }
    }
}