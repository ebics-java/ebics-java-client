package org.ebics.client.console.h005;

import org.apache.commons.cli.*;
import org.ebics.client.api.EbicsModel;
import org.ebics.client.api.User;
import org.ebics.client.console.ConsoleAppBase;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.exception.NoDownloadDataAvailableException;
import org.ebics.client.filetransfer.h005.FileTransfer;
import org.ebics.client.io.IOUtils;
import org.ebics.client.keymgmt.h005.KeyManagementImpl;
import org.ebics.client.messages.Messages;
import org.ebics.client.order.EbicsService;
import org.ebics.client.order.h005.EbicsDownloadOrder;
import org.ebics.client.order.h005.EbicsUploadOrder;
import org.ebics.client.session.EbicsSession;
import org.ebics.client.session.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleApp {

    private static Logger logger = LoggerFactory.getLogger(ConsoleApp.class);

    private ConsoleAppBase app;
    private CommandLine cmd;

    private ConsoleApp(File rootDir, File defaultEbicsConfigFile, CommandLine cmd) throws IOException {
        app = ConsoleAppBase.createConsoleApp(rootDir, defaultEbicsConfigFile);
        this.cmd = cmd;
    }
    
    private EbicsModel getEbicsModel() {
        return app.getEbicsModel();
    }
    
    private User getDefaultUser() {
        return app.getDefaultUser();
    }

    private Product getDefaultProduct() {
        return app.getDefaultProduct();
    }

    private static CommandLine parseArguments(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        options.addOption(null, "help", false, "Print this help text");
        CommandLine line = parser.parse(options, args);
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            System.out.println();
            formatter.printHelp(EbicsModel.class.getSimpleName(), options);
            System.out.println();
            System.exit(0);
        }
        return line;
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(null, "letters", false, "Create INI Letters");
        options.addOption(null, "create", false, "Create user keys and initialize EBICS user");

        options.addOption(null, "listUsers", false, "List stored user ids");
        options.addOption(null, "listPartners", false, "List stored partner ids");
        options.addOption(null, "listBank", false, "List stored bank ids");

        options.addOption(null, "skip_order", true, "Skip a number of order ids");

        options.addOption("o", "output", true, "Output file for EBICS download");
        options.addOption("i", "input", true, "Input file for EBICS upload");

        options.addOption("p", "params", true, "key:value array of string parameters for upload or download request, example FORMAT:pain.001 TEST:TRUE EBCDIC:TRUE");
        options.addOption("s", "start", true, "Download request starting with date");
        options.addOption("e", "end", true, "Download request ending with date");

        options.addOption("ns", "no-signature", false, "Don't provide electronic signature for EBICS upload (ES flag=false, OrderAttribute=DZHNN)");

        //EBICS 2.4/2.5/3.0 admin order type
        options.addOption("at", "admin-type", true, "EBICS 2.4/2.5/3.0 admin order type (INI, HIA, HPB, SPR)");
        //EBICS 3.0 parameters
        options.addOption("btf", "business-transaction-format", true, "EBICS 3.0 BTF service given by following pattern:\nSERVICE NAME:[OPTION]:[SCOPE]:[container]:message name:[variant]:[version]:[FORMAT]\nfor example: GLB::CH:zip:camt.054:001:03:XML");

        options.addOption("sn", "service-name", true, "EBICS 3.0 Name of service, example 'SCT' (SEPA credit transfer)");
        options.addOption("so", "service-option", true, "EBICS 3.0 Optional characteristic(s) of a service Example: “URG” = urgent");
        options.addOption("ss", "service-scope", true, "EBICS 3.0 Specifies whose rules have to be taken into account for the service. 2-char country codes, 3-char codes for other scopes “BIL” means bilaterally agreed");
        options.addOption("ct", "service-container", true, "EBICS 3.0 The container type if required (SVC, XML, ZIP)");
        options.addOption("mn", "service-message-name", true, "EBICS 3.0 Service message name, for example pain.001 or mt103");
        options.addOption("mve", "service-message-version", true, "EBICS 3.0 Service message version for ISO formats, for example 03");
        options.addOption("mva", "service-message-variant", true, "EBICS 3.0 Service message variant for ISO formats, for example 001");
        options.addOption("mf", "service-message-format", true, "EBICS 3.0 Service message format, for example XML, JSON, PFD, ASN1");

        CommandLine cmd = parseArguments(options, args);

        File defaultRootDir = new File(System.getProperty("user.home") + File.separator + "ebics"
                + File.separator + "client");
        File defaultEbicsConfigFile = new File(defaultRootDir, "ebics.txt");
        new ConsoleApp(defaultRootDir, defaultEbicsConfigFile, cmd).runMain();
    }

    private void runMain() throws Exception {
        if (cmd.hasOption("listUsers")) {
            logger.info(Messages.getString("list.user.ids", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, getEbicsModel().listUserId().toString()));
        }
        if (cmd.hasOption("listBanks")) {
            logger.info(Messages.getString("list.bank.ids", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, getEbicsModel().listBankId().toString()));
        }
        if (cmd.hasOption("listPartners")) {
            logger.info(Messages.getString("list.partner.ids", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, getEbicsModel().listPartnerId().toString()));
        }

        if (cmd.hasOption("create")) {
            app.createDefaultUser();
        } else {
            app.loadDefaultUser();
        }

        if (cmd.hasOption("letters")) {
            getEbicsModel().createLetters(getDefaultUser(), false);
        }

        EbicsSession session = getEbicsModel().createSession(getDefaultUser(), getDefaultProduct());

        //Administrative order types processing
        if (cmd.hasOption("at")) {
            {
                String adminOrderType = cmd.getOptionValue("at");
                switch (adminOrderType) {
                    case "INI":
                        sendINIRequest(getDefaultUser(), session);
                        break;
                    case "HIA":
                        sendHIARequest(getDefaultUser(), session);
                        break;
                    case "HPB":
                        sendHPBRequest(getDefaultUser(), session);
                        break;
                    case "SPR":
                        revokeSubscriber(getDefaultUser(), session);
                        break;
                    default:
                        logger.error(Messages.getString("unknown.admin.ordertype", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, adminOrderType));
                }
            }
        }

        //Download file
        if (cmd.hasOption('o')) {
            EbicsDownloadOrder downloadOrder = readDownloadOder();
            fetchFile(getOutputFile(cmd.getOptionValue('o')), session, downloadOrder, false);
        }

        //Upload file
        if (cmd.hasOption('i')) {
            String inputFileValue = cmd.getOptionValue("i");
            EbicsUploadOrder uploadOrder = readUploadOrder(inputFileValue);
            sendFile(new File(inputFileValue), session, uploadOrder);
        }

        if (cmd.hasOption("skip_order")) {
            int count = Integer.parseInt(cmd.getOptionValue("skip_order"));
            while (count-- > 0) {
                getDefaultUser().getPartner().nextOrderId();
            }
        }

        getEbicsModel().saveAll();
    }

    private EbicsDownloadOrder readDownloadOder() throws java.text.ParseException {
        final Map<String, String> params = readParams(cmd.getOptionValues("p"));
        final Date start = readDate( 's');
        final Date end = readDate( 'e');
        return new EbicsDownloadOrder(readEbicsService(), start, end, params);
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
    private EbicsService readEbicsService() {
        //sn Service name: BCT
        //so Service option: -
        //ss Service scope: CH
        //ct Container type: zip / xml / svc (enum)
        //mn Message name: pain.001
        //mva: Message variant: - / 001
        //mve: Message version: 03
        //mf: Message format: -
        if (cmd.hasOption("btf")) {
            String btf = cmd.getOptionValue("btf");
            String regex = "([A-Z0-9]{3}):([A-Z0-9]{3,10})?:([A-Z0-9]{2,3})?:(zip|xml|svc)?:([a-z\\.0-9]{1,10}):([0-9]{3})?:([0-9]{2})?:([A-Z0-9]{1,4})?";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(cmd.getOptionValue("btf"));
            if (matcher.find()) {
                return new EbicsService(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4),
                        matcher.group(5), matcher.group(6), matcher.group(7), matcher.group(8));
            } else {
                String errorMessage = Messages.getString("btf.parse.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, btf);
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        } else {
            return new EbicsService(cmd.getOptionValue("sn"), cmd.getOptionValue("so"), cmd.getOptionValue("ss"), cmd.getOptionValue("ct"),
                    cmd.getOptionValue("mn"), cmd.getOptionValue("mva"), cmd.getOptionValue("mve"), cmd.getOptionValue("mf"));
        }
    }

    private Date readDate(char dateParam) throws java.text.ParseException {
        if (cmd.hasOption(dateParam)) {
            String inputDate = cmd.getOptionValue(dateParam);
            try {
                return new SimpleDateFormat("dd/MM/yyyy").parse(inputDate);
            } catch (java.text.ParseException e) {
                logger.error(Messages.getString("download.date.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, inputDate), e);
                throw e;
            }
        } else {
            return null;
        }
    }

    private EbicsUploadOrder readUploadOrder(String inputFileValue) {
        final Map<String, String> params = readParams(cmd.getOptionValues("p"));
        return new EbicsUploadOrder(readEbicsService(), !cmd.hasOption("ns"), inputFileValue, params);
    }

    private Map<String, String> readParams(String[] paramPairs) {
        if (paramPairs == null)
            return new HashMap<>(0);
        else {
            final Map<String, String> paramMap = new HashMap<>(paramPairs.length);
            for (String paramPair : paramPairs) {
                String[] keyValArr = paramPair.split(":");
                if (keyValArr.length != 2)
                    throw new IllegalArgumentException(String.format("The key value pair '%s' must have one separator ':'", paramPair));
                paramMap.put(keyValArr[0], keyValArr[1]);
            }
            return paramMap;
        }
    }


    private File getOutputFile(String outputFileName) {
        if (outputFileName == null || outputFileName.isEmpty()) {
            throw new IllegalArgumentException("outputFileName not set");
        }
        File file = new File(outputFileName);
        if (file.exists()) {
            throw new IllegalArgumentException("file already exists " + file);
        }
        return file;
    }

    /**
     * Sends a file to the EBICS bank server
     *
     * @param session the EBICS session
     * @throws Exception
     */
    public void sendFile(File file, EbicsSession session, EbicsUploadOrder uploadOrder) throws Exception {
        final FileTransfer transferManager = new FileTransfer(session);
        try {
            transferManager.sendFile(IOUtils.getFileContent(file), uploadOrder);
        } catch (IOException | EbicsException e) {
            logger.error(
                    Messages.getString("upload.file.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME,
                            file.getAbsolutePath()), e);
            throw e;
        }
    }

    public void fetchFile(File file, EbicsSession session, EbicsDownloadOrder downloadOrder,
                                 boolean isTest) throws IOException, EbicsException {

        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }
        final FileTransfer transferManager = new FileTransfer(session);

        try {
            transferManager.fetchFile(downloadOrder, file);
        } catch (NoDownloadDataAvailableException e) {
            // don't log this exception as an error, caller can decide how to handle
            throw e;
        } catch (Exception e) {
            logger.error(
                    Messages.getString("download.file.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME), e);
            throw e;
        }
    }

    /**
     * Sends an INI request to the ebics bank server
     *
     * @param user    the user ID
     * @param session the EBICS session
     * @throws Exception
     */
    public void sendINIRequest(User user, EbicsSession session) throws Exception {
        String userId = user.getUserId();
        logger.info(
                Messages.getString("ini.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId));
        if (user.isInitialized()) {
            logger.info(
                    Messages.getString("user.already.initialized", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME,
                            userId));
            return;
        }

        try {
            new KeyManagementImpl(session).sendINI(null);
            user.setInitialized(true);
            logger.info(
                    Messages.getString("ini.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId));
        } catch (Exception e) {
            logger.error(
                    Messages.getString("ini.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e);
            throw e;
        }
    }

    /**
     * Sends a HIA request to the ebics server.
     *
     * @param user    the user ID.
     * @param session the EBICS session
     * @throws Exception
     */
    public void sendHIARequest(User user, EbicsSession session) throws Exception {
        String userId = user.getUserId();
        logger.info(
                Messages.getString("hia.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId));
        if (user.isInitializedHIA()) {
            logger.info(
                    Messages.getString("user.already.hia.initialized",
                            ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId));
            return;
        }

        try {
            new KeyManagementImpl(session).sendHIA(null);
            user.setInitializedHIA(true);
        } catch (Exception e) {
            logger.error(
                    Messages.getString("hia.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e);
            throw e;
        }
        logger.info(
                Messages.getString("hia.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId));
    }

    /**
     * Sends a HPB request to the ebics server.
     *
     * @param session the EBICS session
     */
    public void sendHPBRequest(User user, EbicsSession session) throws Exception {
        String userId = user.getUserId();
        logger.info(
                Messages.getString("hpb.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId));

        try {
            new KeyManagementImpl(session).sendHPB();
            logger.info(
                    Messages.getString("hpb.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId));
        } catch (Exception e) {
            logger.error(
                    Messages.getString("hpb.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e);
            throw e;
        }
    }

    /**
     * Sends the SPR order to the bank.
     *
     * @param user    the user ID
     * @param session the EBICS session
     * @throws Exception
     */
    public void revokeSubscriber(User user, EbicsSession session) throws Exception {
        String userId = user.getUserId();

        logger.info(
                Messages.getString("spr.request.send", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId));

        try {
            new KeyManagementImpl(session).lockAccess();
        } catch (Exception e) {
            logger.error(
                    Messages.getString("spr.send.error", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId), e);
            throw e;
        }

        logger.info(
                Messages.getString("spr.send.success", ConsoleAppBase.CONSOLE_APP_BUNDLE_NAME, userId));
    }
}
