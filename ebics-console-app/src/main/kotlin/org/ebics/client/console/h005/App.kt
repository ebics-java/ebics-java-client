package org.ebics.client.console.h005

import org.apache.commons.cli.*
import org.ebics.client.api.EbicsModel
import org.ebics.client.api.User
import org.ebics.client.console.ConsoleAppBase
import org.ebics.client.console.ConsoleAppBase.Companion.createConsoleApp
import org.ebics.client.exception.EbicsException
import org.ebics.client.exception.NoDownloadDataAvailableException
import org.ebics.client.filetransfer.h005.FileTransfer
import org.ebics.client.io.IOUtils
import org.ebics.client.keymgmt.h005.KeyManagementImpl
import org.ebics.client.messages.Messages
import org.ebics.client.order.EbicsService
import org.ebics.client.order.h005.EbicsDownloadOrder
import org.ebics.client.order.h005.EbicsUploadOrder
import org.ebics.client.session.EbicsSession
import org.ebics.client.session.Product
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.system.exitProcess

class ConsoleApp(private val cmd: CommandLine) {
    private val app: ConsoleAppBase = createConsoleApp()
    private val ebicsModel: EbicsModel
        get() = app.ebicsModel
    private val defaultUser: User?
        get() = app.defaultUser
    private val defaultProduct: Product
        get() = app.defaultProduct

    @Throws(Exception::class)
    fun runMain() {
        app.loadDefaultUser()

        val session = ebicsModel.createSession(defaultUser, defaultProduct)

        //Administrative order types processing
        if (cmd.hasOption("at")) {
            run {
                when (val adminOrderType = cmd.getOptionValue("at")) {
                    "INI" -> sendINIRequest(defaultUser, session)
                    "HIA" -> sendHIARequest(defaultUser, session)
                    "HPB" -> sendHPBRequest(defaultUser, session)
                    "SPR" -> revokeSubscriber(defaultUser, session)
                    else -> logger.error(Messages.getString("unknown.admin.ordertype", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, adminOrderType))
                }
            }
        }

        //Download file
        if (cmd.hasOption('o')) {
            val downloadOrder = readDownloadOder()
            fetchFile(getOutputFile(cmd.getOptionValue('o')), session, downloadOrder, false)
        }

        //Upload file
        if (cmd.hasOption('i')) {
            val inputFileValue = cmd.getOptionValue("i")
            val uploadOrder = readUploadOrder(inputFileValue)
            sendFile(File(inputFileValue), session, uploadOrder)
        }

        ebicsModel.saveAll()
    }

    @Throws(ParseException::class)
    private fun readDownloadOder(): EbicsDownloadOrder {
        val params = readParams(cmd.getOptionValues("p"))
        val start = readDate('s')
        val end = readDate('e')
        return EbicsDownloadOrder(readEbicsService(), start, end, params)
    }

    /**
     * Parse EBICS 3.0 BTF from command line to EbicsService
     * BTF String pattern:
     * SERVICE NAME:[OPTION]:[SCOPE]:[container]:message name:[variant]:[version]:[variant]:[FORMAT]
     * Example BTFs in one string:
     * GLB::CH:zip:camt.054:001:03:XML
     * BTC::CH:xml:pain.001:001:09:XML
     * BTC:URG:DE:xml:pain.001:001:09:XML
     *
     * @return
     */
    private fun readEbicsService(): EbicsService {
        //sn Service name: BCT
        //so Service option: -
        //ss Service scope: CH
        //ct Container type: zip / xml / svc (enum)
        //mn Message name: pain.001
        //mva: Message variant: - / 001
        //mve: Message version: 03
        //mf: Message format: -
        return if (cmd.hasOption("btf")) {
            val btf = cmd.getOptionValue("btf")
            val regex = "([A-Z0-9]{3}):([A-Z0-9]{3,10})?:([A-Z0-9]{2,3})?:(zip|xml|svc)?:([a-z\\.0-9]{1,10}):([0-9]{3})?:([0-9]{2})?:([A-Z0-9]{1,4})?"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(cmd.getOptionValue("btf"))
            if (matcher.find()) {
                EbicsService(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4),
                        matcher.group(5), matcher.group(6), matcher.group(7), matcher.group(8))
            } else {
                val errorMessage = Messages.getString("btf.parse.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, btf)
                logger.error(errorMessage)
                throw IllegalArgumentException(errorMessage)
            }
        } else {
            EbicsService(cmd.getOptionValue("sn"), cmd.getOptionValue("so"), cmd.getOptionValue("ss"), cmd.getOptionValue("ct"),
                    cmd.getOptionValue("mn"), cmd.getOptionValue("mva"), cmd.getOptionValue("mve"), cmd.getOptionValue("mf"))
        }
    }

    @Throws(ParseException::class)
    private fun readDate(dateParam: Char): Date? {
        return if (cmd.hasOption(dateParam)) {
            val inputDate = cmd.getOptionValue(dateParam)
            try {
                SimpleDateFormat("dd/MM/yyyy").parse(inputDate)
            } catch (e: ParseException) {
                logger.error(Messages.getString("download.date.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, inputDate), e)
                throw e
            }
        } else {
            null
        }
    }

    private fun readUploadOrder(inputFileValue: String): EbicsUploadOrder {
        val params = readParams(cmd.getOptionValues("p"))
        return EbicsUploadOrder(readEbicsService(), !cmd.hasOption("ns"), !cmd.hasOption("neds"), inputFileValue, params)
    }

    private fun readParams(paramPairs: Array<String>?): Map<String, String> {
        return if (paramPairs == null) HashMap(0) else {
            val paramMap: MutableMap<String, String> = HashMap(paramPairs.size)
            for (paramPair in paramPairs) {
                val keyValArr = paramPair.split(":".toRegex()).toTypedArray()
                require(keyValArr.size == 2) { String.format("The key value pair '%s' must have one separator ':'", paramPair) }
                paramMap[keyValArr[0]] = keyValArr[1]
            }
            paramMap
        }
    }

    private fun getOutputFile(outputFileName: String?): File {
        require(!(outputFileName == null || outputFileName.isEmpty())) { "outputFileName not set" }
        val file = File(outputFileName)
        require(!file.exists()) { "file already exists $file" }
        return file
    }

    /**
     * Sends a file to the EBICS bank server
     *
     * @param session the EBICS session
     * @throws Exception
     */
    @Throws(Exception::class)
    fun sendFile(file: File, session: EbicsSession?, uploadOrder: EbicsUploadOrder?) {
        val transferManager = FileTransfer(session)
        try {
            transferManager.sendFile(IOUtils.getFileContent(file), uploadOrder)
        } catch (e: IOException) {
            logger.error(
                    Messages.getString("upload.file.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME,
                            file.absolutePath), e)
            throw e
        } catch (e: EbicsException) {
            logger.error(
                    Messages.getString("upload.file.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME,
                            file.absolutePath), e)
            throw e
        }
    }

    @Throws(IOException::class, EbicsException::class)
    fun fetchFile(file: File?, session: EbicsSession, downloadOrder: EbicsDownloadOrder?,
                  isTest: Boolean) {
        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct")
        if (isTest) {
            session.addSessionParam("TEST", "true")
        }
        val transferManager = FileTransfer(session)
        try {
            transferManager.fetchFile(downloadOrder, file)
        } catch (e: NoDownloadDataAvailableException) {
            // don't log this exception as an error, caller can decide how to handle
            throw e
        } catch (e: Exception) {
            logger.error(
                    Messages.getString("download.file.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME), e)
            throw e
        }
    }

    /**
     * Sends an INI request to the ebics bank server
     *
     * @param user    the user ID
     * @param session the EBICS session
     * @throws Exception
     */
    @Throws(Exception::class)
    fun sendINIRequest(user: User?, session: EbicsSession?) {
        val userId = user!!.userId
        logger.info(
                Messages.getString("ini.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
        if (user.isInitialized) {
            logger.info(
                    Messages.getString("user.already.initialized", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME,
                            userId))
            return
        }
        try {
            KeyManagementImpl(session).sendINI(null)
            user.isInitialized = true
            logger.info(
                    Messages.getString("ini.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
        } catch (e: Exception) {
            logger.error(
                    Messages.getString("ini.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e)
            throw e
        }
    }

    /**
     * Sends a HIA request to the ebics server.
     *
     * @param user    the user ID.
     * @param session the EBICS session
     * @throws Exception
     */
    @Throws(Exception::class)
    fun sendHIARequest(user: User?, session: EbicsSession?) {
        val userId = user!!.userId
        logger.info(
                Messages.getString("hia.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
        if (user.isInitializedHIA) {
            logger.info(
                    Messages.getString("user.already.hia.initialized",
                            ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
            return
        }
        try {
            KeyManagementImpl(session).sendHIA(null)
            user.isInitializedHIA = true
        } catch (e: Exception) {
            logger.error(
                    Messages.getString("hia.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e)
            throw e
        }
        logger.info(
                Messages.getString("hia.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
    }

    /**
     * Sends a HPB request to the ebics server.
     *
     * @param session the EBICS session
     */
    @Throws(Exception::class)
    fun sendHPBRequest(user: User?, session: EbicsSession?) {
        val userId = user!!.userId
        logger.info(
                Messages.getString("hpb.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
        try {
            KeyManagementImpl(session).sendHPB()
            logger.info(
                    Messages.getString("hpb.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
        } catch (e: Exception) {
            logger.error(
                    Messages.getString("hpb.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e)
            throw e
        }
    }

    /**
     * Sends the SPR order to the bank.
     *
     * @param user    the user ID
     * @param session the EBICS session
     * @throws Exception
     */
    @Throws(Exception::class)
    fun revokeSubscriber(user: User?, session: EbicsSession?) {
        val userId = user!!.userId
        logger.info(
                Messages.getString("spr.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
        try {
            KeyManagementImpl(session).lockAccess()
        } catch (e: Exception) {
            logger.error(
                    Messages.getString("spr.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e)
            throw e
        }
        logger.info(
                Messages.getString("spr.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConsoleApp::class.java)
    }

}

fun main(args: Array<String>) {
    val options = createCmdOptions()
    val cmd = parseArguments(options, args)
    ConsoleApp(cmd).runMain()
}

private fun parseArguments(options: Options, args: Array<String>): CommandLine {
    val parser: CommandLineParser = DefaultParser()
    options.addOption(null, "help", false, "Print this help text")
    val line = parser.parse(options, args)
    if (line.hasOption("help")) {
        val formatter = HelpFormatter()
        println()
        formatter.printHelp(ConsoleApp::class.java.simpleName, options)
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
    options.addOption(null, "skip_order", true, "Skip a number of order ids")
    options.addOption("o", "output", true, "Output file for EBICS download")
    options.addOption("i", "input", true, "Input file for EBICS upload")
    options.addOption("p", "params", true, "key:value array of string parameters for upload or download request, example FORMAT:pain.001 TEST:TRUE EBCDIC:TRUE")
    options.addOption("s", "start", true, "Download request starting with date")
    options.addOption("e", "end", true, "Download request ending with date")
    options.addOption("ns", "no-signature", false, "Don't provide electronic signature for EBICS upload (ES flag=false, equivalent of OrderAttribute=DZHNN)")
    options.addOption("neds", "no-eds", false, "Don't request EDS (electronic distributed signature) for EBICS upload only when (no-signature is not used = ES flag=true)")

    //EBICS 2.4/2.5/3.0 admin order type
    options.addOption("at", "admin-type", true, "EBICS admin order type (INI, HIA, HPB, SPR)")
    //EBICS 3.0 parameters
    options.addOption("btf", "business-transaction-format", true, "EBICS 3.0 BTF service given by following pattern:\nSERVICE NAME:[OPTION]:[SCOPE]:[container]:message name:[variant]:[version]:[FORMAT]\nfor example: GLB::CH:zip:camt.054:001:03:XML")
    options.addOption("sn", "service-name", true, "EBICS 3.0 Name of service, example 'SCT' (SEPA credit transfer)")
    options.addOption("so", "service-option", true, "EBICS 3.0 Optional characteristic(s) of a service Example: “URG” = urgent")
    options.addOption("ss", "service-scope", true, "EBICS 3.0 Specifies whose rules have to be taken into account for the service. 2-char country codes, 3-char codes for other scopes “BIL” means bilaterally agreed")
    options.addOption("ct", "service-container", true, "EBICS 3.0 The container type if required (SVC, XML, ZIP)")
    options.addOption("mn", "service-message-name", true, "EBICS 3.0 Service message name, for example pain.001 or mt103")
    options.addOption("mve", "service-message-version", true, "EBICS 3.0 Service message version for ISO formats, for example 03")
    options.addOption("mva", "service-message-variant", true, "EBICS 3.0 Service message variant for ISO formats, for example 001")
    options.addOption("mf", "service-message-format", true, "EBICS 3.0 Service message format, for example XML, JSON, PFD, ASN1")
    return options
}