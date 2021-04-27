package org.ebics.client.console

import org.apache.commons.cli.*
import org.ebics.client.api.EbicsModel
import org.ebics.client.api.EbicsVersion
import org.ebics.client.api.User
import org.ebics.client.console.h003.ConsoleApp
import org.ebics.client.interfaces.Configuration
import org.ebics.client.interfaces.PasswordCallback
import org.ebics.client.messages.Messages
import org.ebics.client.session.DefaultConfiguration
import org.ebics.client.session.Product
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.system.exitProcess

/**
 * Ebics client app
 * This is the base part of Console App without being depended on specific EBICS h00X API.
 */
class ConsoleAppBase constructor(
    configuration: Configuration,
    private val properties: ConfigProperties,
    val defaultProduct: Product
) {
    var defaultUser: User? = null
        private set
    val ebicsModel: EbicsModel = EbicsModel(configuration)

    @Throws(Exception::class)
    private fun createUser(properties: ConfigProperties, pwdHandler: PasswordCallback): User {
        val ebicsVersion = EbicsVersion.valueOf(properties["ebicsVersion"])
        val userId = properties["userId"]
        val partnerId = properties["partnerId"]
        val bankUrl = properties["bank.url"]
        val bankName = properties["bank.name"]
        val hostId = properties["hostId"]
        val userName = properties["user.name"]
        val userEmail = properties["user.email"]
        val userCountry = properties["user.country"]
        val userOrg = properties["user.org"]
        val useCertificates = properties["use.certificates"].toBoolean()
        //Certificates in EBICS 3.0 are mandatory, for EBICS 2.4/2.5 optional
        if( ebicsVersion == EbicsVersion.H005)
            require(useCertificates) {"Certificates must be used for EBICS 3.0, make use.certificates=true"}
        return ebicsModel.createUser(
            URL(bankUrl), ebicsVersion, bankName, hostId, partnerId, userId, userName, userEmail,
            userCountry, userOrg, useCertificates, true, pwdHandler
        )
    }

    @Throws(Exception::class)
    fun createDefaultUser() {
        defaultUser = createUser(properties, createPasswordCallback())
    }

    @Throws(Exception::class)
    fun loadDefaultUser() {
        val userId = properties["userId"]
        val hostId = properties["hostId"]
        val partnerId = properties["partnerId"]
        defaultUser = ebicsModel.loadUser(hostId, partnerId, userId, createPasswordCallback())
    }

    private fun createPasswordCallback(): PasswordCallback {
        val password = properties["password"]
        return PasswordCallback { password.toCharArray() }
    }

    fun runMain(cmd: CommandLine) {
        if (cmd.hasOption("listUsers")) {
            logger.info(
                Messages.getString(
                    "list.user.ids",
                    CONSOLE_APP_BUNDLE_NAME,
                    ebicsModel.listUserId().toString()
                )
            )
        }
        if (cmd.hasOption("listBanks")) {
            logger.info(
                Messages.getString(
                    "list.bank.ids",
                    CONSOLE_APP_BUNDLE_NAME,
                    ebicsModel.listBankId().toString()
                )
            )
        }
        if (cmd.hasOption("listPartners")) {
            logger.info(
                Messages.getString(
                    "list.partner.ids",
                    CONSOLE_APP_BUNDLE_NAME,
                    ebicsModel.listPartnerId().toString()
                )
            )
        }

        if (cmd.hasOption("create")) {
            createDefaultUser()
        } else {
            loadDefaultUser()
        }

        if (cmd.hasOption("letters")) {
            ebicsModel.createLetters(defaultUser, false)
        }

        ebicsModel.saveAll()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConsoleAppBase::class.java)
        const val CONSOLE_APP_BUNDLE_NAME = "org.ebics.client.console.messages"

        @JvmStatic
        @Throws(FileNotFoundException::class, IOException::class)
        fun createConsoleApp(): ConsoleAppBase {
            val defaultRootDir = File(
                System.getProperty("user.home") + File.separator + "ebics"
                        + File.separator + "client"
            )
            val defaultEbicsConfigFile = File(defaultRootDir, "ebics.txt")
            val properties = ConfigProperties(defaultEbicsConfigFile)
            val country = properties["countryCode"].toUpperCase()
            val language = properties["languageCode"].toLowerCase()
            val productName = properties["productName"]
            val locale = Locale(language, country)
            val configuration: DefaultConfiguration = object : DefaultConfiguration(defaultRootDir.absolutePath) {
                override fun getLocale(): Locale {
                    return locale
                }
            }
            val product = Product(productName, language, null)
            return ConsoleAppBase(configuration, properties, product)
        }
    }
}

fun main(args: Array<String>) {
    val options = createCmdOptions()
    val cmd = parseArguments(options, args)
    ConsoleAppBase.createConsoleApp().runMain(cmd)
}

private fun parseArguments(options: Options, args: Array<String>): CommandLine {
    val parser: CommandLineParser = DefaultParser()
    options.addOption(null, "help", false, "Print this help text")
    val line = parser.parse(options, args)
    if (line.hasOption("help")) {
        val formatter = HelpFormatter()
        println()
        formatter.printHelp(ConsoleAppBase::class.java.simpleName, options)
        println()
        exitProcess(0)
    }
    return line
}

private fun createCmdOptions(): Options {
    val options = Options()
    options.addOption(null, "letters", false, "Create INI Letters")
    options.addOption(null, "create", false, "Create user keys and initialize EBICS user")
    options.addOption(null, "listUsers", false, "List stored user ids")
    options.addOption(null, "listPartners", false, "List stored partner ids")
    options.addOption(null, "listBank", false, "List stored bank ids")
    return options
}