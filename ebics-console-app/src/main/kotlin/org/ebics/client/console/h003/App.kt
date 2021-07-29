package org.ebics.client.console.h003

import org.apache.commons.cli.*
import org.ebics.client.console.ConsoleAppBase
import org.ebics.client.console.ConsoleAppBase.Companion.createConsoleApp
import org.ebics.client.exception.EbicsException
import org.ebics.client.exception.NoDownloadDataAvailableException
import org.ebics.client.file.EbicsFileModel
import org.ebics.client.file.FileConfiguration
import org.ebics.client.file.User
import org.ebics.client.filetransfer.h003.FileTransfer

import org.ebics.client.io.IOUtils
import org.ebics.client.keymgmt.h003.KeyManagementImpl
import org.ebics.client.messages.Messages
import org.ebics.client.api.EbicsSession
import org.ebics.client.api.EbicsUser
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.AttributeType
import org.ebics.client.order.h003.EbicsDownloadOrder
import org.ebics.client.order.h003.EbicsUploadOrder
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class ConsoleApp(rootDir: File, defaultEbicsConfigFile: File, private val cmd: CommandLine) {
    private val app: ConsoleAppBase = createConsoleApp(rootDir, defaultEbicsConfigFile)
    private val ebicsModel: EbicsFileModel
        get() = app.ebicsModel

    @Throws(Exception::class)
    fun runMain() {
        val session = if (cmd.hasOption("create")) {
            app.createDefaultSession(EbicsVersion.H003)
        } else {
            app.loadDefaultSession().apply {
                require(user.ebicsVersion == EbicsVersion.H003)
                { "User was initialized with ${user.ebicsVersion} version, but you are running H003 client" }
            }
        }
        val user = session.user as User
        if (cmd.hasOption("letters")) {
            ebicsModel.createLetters(session.user, session.userCert)
        }

        //Administrative order types processing
        if (cmd.hasOption("at")) {
            run {
                when (val adminOrderType = cmd.getOptionValue("at")) {
                    "INI" -> sendINIRequest(user, session)
                    "HIA" -> sendHIARequest(user, session)
                    "HPB" -> sendHPBRequest(user, session, app.getPassword())
                    "SPR" -> revokeSubscriber(user, session)
                    else -> logger.error(
                        Messages.getString(
                            "unknown.admin.ordertype",
                            ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME,
                            adminOrderType
                        )
                    )
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
            val uploadOrder = readUploadOrder()
            sendFile(File(inputFileValue), session, uploadOrder)
        }
        if (cmd.hasOption("skip_order")) {
            var count = cmd.getOptionValue("skip_order").toInt()
            while (count-- > 0) {
                user.partner.nextOrderId()
            }
        }
        ebicsModel.saveUser(user)
    }

    @Throws(ParseException::class)
    private fun readDownloadOder(): EbicsDownloadOrder {
        val params = readParams(cmd.getOptionValues("p"))
        val start = readDate('s')
        val end = readDate('e')
        val orderType = readOrderType()
        return if (orderType == "FDL") EbicsDownloadOrder(null, start, end, params) else EbicsDownloadOrder(
            readOrderType(),
            start,
            end,
            params
        )
    }

    private fun readOrderType(): String {
        if (cmd.hasOption("ot")) {
            return cmd.getOptionValue("ot")
        }
        throw IllegalArgumentException("For option -i/-o must be upload/download order type specified for example -ot XE2")
    }

    @Throws(ParseException::class)
    private fun readDate(dateParam: Char): Date? {
        return if (cmd.hasOption(dateParam)) {
            val inputDate = cmd.getOptionValue(dateParam)
            try {
                SimpleDateFormat("dd/MM/yyyy").parse(inputDate)
            } catch (e: ParseException) {
                logger.error(
                    Messages.getString(
                        "download.date.error",
                        ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME,
                        inputDate
                    ), e
                )
                throw e
            }
        } else {
            null
        }
    }

    @Throws(Exception::class)
    private fun readUploadOrder(): EbicsUploadOrder {
        val params = readParams(cmd.getOptionValues("p"))
        val orderType = readOrderType()
        return if (orderType == "FUL") EbicsUploadOrder(
            null,
            if (cmd.hasOption("ns")) AttributeType.DZHNN else AttributeType.OZHNN,
            params
        ) else EbicsUploadOrder(
            readOrderType(),
            if (cmd.hasOption("ns")) AttributeType.DZHNN else AttributeType.OZHNN,
            params
        )
    }

    private fun readParams(paramPairs: Array<String>?): Map<String, String> {
        return if (paramPairs == null) HashMap(0) else {
            val paramMap: MutableMap<String, String> = HashMap(paramPairs.size)
            for (paramPair in paramPairs) {
                val keyValArr = paramPair.split(":".toRegex())
                require(keyValArr.size == 2) {
                    String.format(
                        "The key value pair '%s' must have one separator ':'",
                        paramPair
                    )
                }
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
    fun sendFile(file: File, session: EbicsSession, uploadOrder: EbicsUploadOrder) {
        val transferManager = FileTransfer(session)
        try {
            transferManager.sendFile(IOUtils.getFileContent(file), uploadOrder)
        } catch (e: IOException) {
            logger.error(
                Messages.getString(
                    "upload.file.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME,
                    file.absolutePath
                ), e
            )
            throw e
        } catch (e: EbicsException) {
            logger.error(
                Messages.getString(
                    "upload.file.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME,
                    file.absolutePath
                ), e
            )
            throw e
        }
    }

    @Throws(IOException::class, EbicsException::class)
    fun fetchFile(
        file: File, session: EbicsSession, downloadOrder: EbicsDownloadOrder,
        isTest: Boolean
    ) {
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
                Messages.getString("download.file.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME), e
            )
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
    fun sendINIRequest(user: EbicsUser, session: EbicsSession) {
        val userId = user.userId
        logger.info(
            Messages.getString("ini.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId)
        )
        try {
            KeyManagementImpl(session).sendINI(null)
            logger.info(Messages.getString("ini.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId))
        } catch (e: Exception) {
            logger.error(
                Messages.getString("ini.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e
            )
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
    fun sendHIARequest(user: EbicsUser, session: EbicsSession) {
        val userId = user.userId
        logger.info(
            Messages.getString("hia.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId)
        )
        try {
            KeyManagementImpl(session).sendHIA(null)
        } catch (e: Exception) {
            logger.error(
                Messages.getString("hia.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e
            )
            throw e
        }
        logger.info(
            Messages.getString("hia.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId)
        )
    }

    /**
     * Sends a HPB request to the ebics server.
     *
     * @param session the EBICS session
     */
    @Throws(Exception::class)
    fun sendHPBRequest(user: EbicsUser, session: EbicsSession, password: String) {
        val userId = user.userId
        logger.info(
            Messages.getString("hpb.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId)
        )
        try {
            val manager = KeyManagementImpl(session).sendHPB(password)
            manager.save(FileOutputStream((session.configuration as FileConfiguration).getKeystoreDirectory(user) + File.separator + session.getBankID() + ".p12"))
            logger.info(
                Messages.getString("hpb.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId)
            )
        } catch (e: Exception) {
            logger.error(
                Messages.getString("hpb.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e
            )
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
    fun revokeSubscriber(user: EbicsUser, session: EbicsSession) {
        val userId = user.userId
        logger.info(
            Messages.getString("spr.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId)
        )
        try {
            KeyManagementImpl(session).lockAccess()
        } catch (e: Exception) {
            logger.error(
                Messages.getString("spr.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e
            )
            throw e
        }
        logger.info(
            Messages.getString("spr.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId)
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConsoleApp::class.java)
    }

}

fun main(args: Array<String>) {
    val options = createCmdOptions()
    val cmd = parseArguments(options, args)
    val defaultRootDir = File(
        System.getProperty("user.home") + File.separator + "ebics"
                + File.separator + "client"
    )
    val defaultEbicsConfigFile = File(defaultRootDir, "ebics.txt")
    ConsoleApp(defaultRootDir, defaultEbicsConfigFile, cmd).runMain()
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
    options.addOption(
        "p",
        "params",
        true,
        "key:value array of string parameters for upload or download request, example FORMAT:pain.001 TEST:TRUE EBCDIC:TRUE"
    )
    options.addOption("s", "start", true, "Download request starting with date")
    options.addOption("e", "end", true, "Download request ending with date")
    options.addOption(
        "ns",
        "no-signature",
        false,
        "Don't provide electronic signature for EBICS upload (ES flag=false, OrderAttribute=DZHNN)"
    )

    //EBICS 2.4/2.5/3.0 admin order type
    options.addOption("at", "admin-type", true, "EBICS admin order type (INI, HIA, HPB, SPR)")
    //EBICS 2.4/2.5 business order type
    options.addOption("ot", "order-type", true, "EBICS business order type like(XE2, XE3, CCT, CDD,..)")
    return options
}