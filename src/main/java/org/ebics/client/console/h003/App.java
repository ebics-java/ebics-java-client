package org.ebics.client.console.h003;

import org.apache.commons.cli.*;
import org.ebics.client.api.AbstractEbicsClient;
import org.ebics.client.api.h003.EbicsClient;
import org.ebics.client.interfaces.EbicsLogger;
import org.ebics.client.messages.Messages;
import org.ebics.client.order.EbicsService;
import org.ebics.client.order.h003.EbicsDownloadOrder;
import org.ebics.client.order.h003.EbicsUploadOrder;
import org.ebics.client.utils.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    private static CommandLine parseArguments(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        options.addOption(null, "help", false, "Print this help text");
        CommandLine line = parser.parse(options, args);
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            System.out.println();
            formatter.printHelp(AbstractEbicsClient.class.getSimpleName(), options);
            System.out.println();
            System.exit(0);
        }
        return line;
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(null, "letters", false, "Create INI Letters");
        options.addOption(null, "create", false, "Create user keys and initialize EBICS user");

        options.addOption(null, "skip_order", true, "Skip a number of order ids");

        options.addOption("o", "output", true, "Output file for EBICS download");
        options.addOption("i", "input", true, "Input file for EBICS upload");

        options.addOption("p", "params", true, "key:value array of string parameters for upload or download request, example FORMAT:pain.001 TEST:TRUE EBCDIC:TRUE");
        options.addOption("s", "start", true, "Download request starting with date");
        options.addOption("e", "end", true, "Download request ending with date");

        options.addOption("ns", "no-signature", false, "Don't provide electronic signature for EBICS upload (ES flag=false, OrderAttribute=DZHNN)");

        //EBICS 2.4/2.5 admin order type
        options.addOption("at", "admin-type", true, "EBICS 2.4/2.5/3.0 admin order type (INI, HIA, HPB, SPR)");
        //EBICS 2.4/2.5 business order type
        options.addOption("ot", "order-type", true, "EBICS 2.4/2.5 business order type like(XE2, XE3, CCT, CDD,..)");
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
        File ebicsClientProperties = new File(defaultRootDir, "ebics.txt");
        EbicsClient client = EbicsClient.createEbicsClient(defaultRootDir, ebicsClientProperties);

        if (cmd.hasOption("create")) {
            client.createDefaultUser();
        } else {
            client.loadDefaultUser();
        }

        if (cmd.hasOption("letters")) {
            client.createLetters(client.getDefaultUser(), false);
        }

        //Administrative order types processing
        if (cmd.hasOption("at")) {
            {
                String adminOrderType = cmd.getOptionValue("at");
                switch (adminOrderType) {
                    case "INI":
                        client.sendINIRequest(client.getDefaultUser(), client.getDefaultProduct());
                        break;
                    case "HIA":
                        client.sendHIARequest(client.getDefaultUser(), client.getDefaultProduct());
                        break;
                    case "HPB":
                        client.sendHPBRequest(client.getDefaultUser(), client.getDefaultProduct());
                        break;
                    case "SPR":
                        client.revokeSubscriber(client.getDefaultUser(), client.getDefaultProduct());
                        break;
                }
            }
        }

        //Download file
        if (cmd.hasOption('o')) {
            EbicsDownloadOrder downloadOrder = readDownloadOder(cmd, client.getConfiguration().getLogger());
            client.fetchFile(getOutputFile(cmd.getOptionValue('o')), client.getDefaultUser(),
                    client.getDefaultProduct(), downloadOrder, false);
        }

        //Upload file
        if (cmd.hasOption('i')) {
            String inputFileValue = cmd.getOptionValue("i");
            EbicsUploadOrder uploadOrder = readUploadOrder(cmd, inputFileValue, client.getConfiguration().getLogger());
            client.sendFile(new File(inputFileValue), client.getDefaultUser(),
                    client.getDefaultProduct(), uploadOrder);
        }

        if (cmd.hasOption("skip_order")) {
            int count = Integer.parseInt(cmd.getOptionValue("skip_order"));
            while (count-- > 0) {
                client.getDefaultUser().getPartner().nextOrderId();
            }
        }

        client.quit();
    }

    private static EbicsDownloadOrder readDownloadOder(CommandLine cmd, EbicsLogger logger) throws java.text.ParseException {
        final Map<String, String> params = readParams(cmd.getOptionValues("p"));
        final Date start = readDate(logger, cmd, 's');
        final Date end = readDate(logger, cmd, 'e');
        String orderType = readOrderType(cmd);
        if (orderType.equals("FDL"))
            return new EbicsDownloadOrder(start, end, params);
        else
            return new EbicsDownloadOrder(readOrderType(cmd), start, end, params);
        //    case H005:
        //        String errorMessage = Messages.getString("ebics.version2x.error", Constants.APPLICATION_BUNDLE_NAME, version.toString());
        //        throw new IllegalArgumentException(errorMessage);
        //}
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
     * @param logger
     * @param cmd
     * @return
     */
    private static EbicsService readEbicsService(CommandLine cmd, EbicsLogger logger) {
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
                String errorMessage = Messages.getString("btf.parse.error", Constants.APPLICATION_BUNDLE_NAME, btf);
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        } else {
            return new EbicsService(cmd.getOptionValue("sn"), cmd.getOptionValue("so"), cmd.getOptionValue("ss"), cmd.getOptionValue("ct"),
                    cmd.getOptionValue("mn"), cmd.getOptionValue("mva"), cmd.getOptionValue("mve"), cmd.getOptionValue("mf"));
        }
    }

    private static String readOrderType(CommandLine cmd) {
        if (cmd.hasOption("ot")) {
            return cmd.getOptionValue("ot");
        }
        throw new IllegalArgumentException("For option -i/-o must be upload/download order type specified for example -ot XE2");
    }

    private static Date readDate(EbicsLogger logger, CommandLine cmd, char dateParam) throws java.text.ParseException {
        if (cmd.hasOption(dateParam)) {
            String inputDate = cmd.getOptionValue(dateParam);
            try {
                return new SimpleDateFormat("dd/MM/yyyy").parse(inputDate);
            } catch (java.text.ParseException e) {
                logger.error(Messages.getString("download.date.error", Constants.APPLICATION_BUNDLE_NAME, inputDate), e);
                throw e;
            }
        } else {
            return null;
        }
    }

    private static EbicsUploadOrder readUploadOrder(CommandLine cmd, String inputFileValue, EbicsLogger logger) throws Exception {
        //final File inputFile = new File(inputFileValue);
        final Map<String, String> params = readParams(cmd.getOptionValues("p"));

        String orderType = readOrderType(cmd);
        if (orderType.equals("FUL"))
            return new EbicsUploadOrder(!cmd.hasOption("ns"), params);
        else
            return new EbicsUploadOrder(readOrderType(cmd), !cmd.hasOption("ns"), params);
        //    case H005:
        //        return new EbicsUploadOrder(readEbicsService(cmd, logger), !cmd.hasOption("ns"), inputFile.getName(), params);
    }

    private static Map<String, String> readParams(String[] paramPairs) {
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


    private static File getOutputFile(String outputFileName) {
        if (outputFileName == null || outputFileName.isEmpty()) {
            throw new IllegalArgumentException("outputFileName not set");
        }
        File file = new File(outputFileName);
        if (file.exists()) {
            throw new IllegalArgumentException("file already exists " + file);
        }
        return file;
    }
}
