/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package org.ebics.client.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ebics.client.filetransfer.FileTransfer;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.exception.NoDownloadDataAvailableException;
import org.ebics.client.keymgmt.KeyManagement;
import org.ebics.client.messages.Messages;
import org.ebics.client.order.EbicsDownloadOrder;
import org.ebics.client.order.EbicsService;
import org.ebics.client.order.EbicsUploadOrder;
import org.ebics.client.session.DefaultConfiguration;
import org.ebics.client.order.EbicsOrderType;
import org.ebics.client.session.Product;
import org.ebics.client.interfaces.Configuration;
import org.ebics.client.interfaces.EbicsBank;
import org.ebics.client.interfaces.EbicsUser;
import org.ebics.client.interfaces.InitLetter;
import org.ebics.client.interfaces.LetterManager;
import org.ebics.client.interfaces.PasswordCallback;
import org.ebics.client.io.IOUtils;
import org.ebics.client.session.EbicsSession;
import org.ebics.client.utils.Constants;

/**
 * The ebics client application. Performs necessary tasks to contact the ebics
 * bank server like sending the INI, HIA and HPB requests for keys retrieval and
 * also performs the files transfer including uploads and downloads.
 *
 */
public class EbicsClient {

    private final Configuration configuration;
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Partner> partners = new HashMap<>();
    private final Map<String, Bank> banks = new HashMap<>();
    private final ConfigProperties properties;
    private Product defaultProduct;
    private User defaultUser;

    static {
        org.apache.xml.security.Init.init();
        java.security.Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Constructs a new ebics client application
     *
     * @param configuration
     *            the application configuration
     * @param properties
     */
    public EbicsClient(Configuration configuration, ConfigProperties properties) {
        this.configuration = configuration;
        this.properties = properties;
        Messages.setLocale(configuration.getLocale());
        configuration.getLogger().info(
            Messages.getString("init.configuration", Constants.APPLICATION_BUNDLE_NAME));
        configuration.init();
    }

    private EbicsSession createSession(User user, Product product) {
        EbicsSession session = new EbicsSession(user, configuration);
        session.setProduct(product);
        return session;
    }

    /**
     * Creates the user necessary directories
     *
     * @param user
     *            the concerned user
     */
    public void createUserDirectories(EbicsUser user) {
        configuration.getLogger().info(
            Messages.getString("user.create.directories", Constants.APPLICATION_BUNDLE_NAME,
                user.getUserId()));
        IOUtils.createDirectories(configuration.getUserDirectory(user));
        IOUtils.createDirectories(configuration.getTransferTraceDirectory(user));
        IOUtils.createDirectories(configuration.getKeystoreDirectory(user));
        IOUtils.createDirectories(configuration.getLettersDirectory(user));
    }

    /**
     * Creates a new EBICS bank with the data you should have obtained from the
     * bank.
     *
     * @param url
     *            the bank URL
     * @param url
     *            the bank name
     * @param hostId
     *            the bank host ID
     * @param useCertificate
     *            does the bank use certificates ?
     * @return the created ebics bank
     */
    private Bank createBank(URL url, String name, String hostId, boolean useCertificate) {
        Bank bank = new Bank(url, name, hostId, useCertificate);
        banks.put(hostId, bank);
        return bank;
    }

    /**
     * Creates a new ebics partner
     *
     * @param bank
     *            the bank
     * @param partnerId
     *            the partner ID
     */
    private Partner createPartner(EbicsBank bank, String partnerId) {
        Partner partner = new Partner(bank, partnerId);
        partners.put(partnerId, partner);
        return partner;
    }

    /**
     * Creates a new ebics user and generates its certificates.
     *
     * @param url
     *            the bank url
     * @param bankName
     *            the bank name
     * @param hostId
     *            the bank host ID
     * @param partnerId
     *            the partner ID
     * @param userId
     *            UserId as obtained from the bank.
     * @param name
     *            the user name,
     * @param email
     *            the user email
     * @param country
     *            the user country
     * @param organization
     *            the user organization or company
     * @param useCertificates
     *            does the bank use certificates ?
     * @param saveCertificates
     *            save generated certificates?
     * @param passwordCallback
     *            a callback-handler that supplies us with the password. This
     *            parameter can be null, in this case no password is used.
     * @return
     * @throws Exception
     */
    public User createUser(URL url, EbicsVersion ebicsVersion, String bankName, String hostId, String partnerId,
        String userId, String name, String email, String country, String organization,
        boolean useCertificates, boolean saveCertificates, PasswordCallback passwordCallback)
        throws Exception {
        configuration.getLogger().info(
            Messages.getString("user.create.info", Constants.APPLICATION_BUNDLE_NAME, userId));

        Bank bank = createBank(url, bankName, hostId, useCertificates);
        Partner partner = createPartner(bank, partnerId);
        try {
            User user = new User(ebicsVersion, partner, userId, name, email, country, organization,
                passwordCallback);
            createUserDirectories(user);
            if (saveCertificates) {
                user.saveUserCertificates(configuration.getKeystoreDirectory(user));
            }
            configuration.getSerializationManager().serialize(bank);
            configuration.getSerializationManager().serialize(partner);
            configuration.getSerializationManager().serialize(user);
            createLetters(user, useCertificates);
            users.put(userId, user);
            partners.put(partner.getPartnerId(), partner);
            banks.put(bank.getHostId(), bank);

            configuration.getLogger().info(
                Messages.getString("user.create.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        } catch (Exception e) {
            configuration.getLogger().error(
                Messages.getString("user.create.error", Constants.APPLICATION_BUNDLE_NAME), e);
            throw e;
        }
    }

    private void createLetters(EbicsUser user, boolean useCertificates)
        throws GeneralSecurityException, IOException, EbicsException, FileNotFoundException {
        user.getPartner().getBank().setUseCertificate(useCertificates);
        LetterManager letterManager = configuration.getLetterManager();
        List<InitLetter> letters = Arrays.asList(letterManager.createA005Letter(user),
            letterManager.createE002Letter(user), letterManager.createX002Letter(user));

        File directory = new File(configuration.getLettersDirectory(user));
        for (InitLetter letter : letters) {
            try (FileOutputStream out = new FileOutputStream(new File(directory, letter.getName()))) {
                letter.writeTo(out);
            }
        }
    }

    /**
     * Loads a user knowing its ID
     *
     * @throws Exception
     */
    public User loadUser(String hostId, String partnerId, String userId,
        PasswordCallback passwordCallback) throws Exception {
        configuration.getLogger().info(
            Messages.getString("user.load.info", Constants.APPLICATION_BUNDLE_NAME, userId));

        try {
            Bank bank;
            Partner partner;
            User user;
            try (ObjectInputStream input = configuration.getSerializationManager().deserialize(
                hostId)) {
                bank = (Bank) input.readObject();
            }
            try (ObjectInputStream input = configuration.getSerializationManager().deserialize(
                "partner-" + partnerId)) {
                partner = new Partner(bank, input);
            }
            try (ObjectInputStream input = configuration.getSerializationManager().deserialize(
                "user-" + userId)) {
                user = new User(partner, input, passwordCallback);
            }
            users.put(userId, user);
            partners.put(partner.getPartnerId(), partner);
            banks.put(bank.getHostId(), bank);
            configuration.getLogger().info(
                Messages.getString("user.load.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        } catch (Exception e) {
            configuration.getLogger().error(
                Messages.getString("user.load.error", Constants.APPLICATION_BUNDLE_NAME), e);
            throw e;
        }
    }

    /**
     * Sends an INI request to the ebics bank server
     *
     * @param user
     *            the user ID
     * @param product
     *            the application product
     * @throws Exception
     */
    public void sendINIRequest(User user, Product product) throws Exception {
        String userId = user.getUserId();
        configuration.getLogger().info(
            Messages.getString("ini.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));
        if (user.isInitialized()) {
            configuration.getLogger().info(
                Messages.getString("user.already.initialized", Constants.APPLICATION_BUNDLE_NAME,
                    userId));
            return;
        }
        EbicsSession session = createSession(user, product);
        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));
        try {
            getKeyManagement(session).sendINI(null);
            user.setInitialized(true);
            configuration.getLogger().info(
                Messages.getString("ini.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
        } catch (Exception e) {
            configuration.getLogger().error(
                Messages.getString("ini.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
            throw e;
        }
    }

    /**
     * Sends a HIA request to the ebics server.
     *
     * @param user
     *            the user ID.
     * @param product
     *            the application product.
     * @throws Exception
     */
    public void sendHIARequest(User user, Product product) throws Exception {
        String userId = user.getUserId();
        configuration.getLogger().info(
            Messages.getString("hia.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));
        if (user.isInitializedHIA()) {
            configuration.getLogger().info(
                Messages.getString("user.already.hia.initialized",
                    Constants.APPLICATION_BUNDLE_NAME, userId));
            return;
        }
        EbicsSession session = createSession(user, product);
        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));
        try {
            getKeyManagement(session).sendHIA(null);
            user.setInitializedHIA(true);
        } catch (Exception e) {
            configuration.getLogger().error(
                Messages.getString("hia.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
            throw e;
        }
        configuration.getLogger().info(
            Messages.getString("hia.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
    }

    /**
     * Sends a HPB request to the ebics server.
     */
    public void sendHPBRequest(User user, Product product) throws Exception {
        String userId = user.getUserId();
        configuration.getLogger().info(
            Messages.getString("hpb.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

        EbicsSession session = createSession(user, product);

        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            getKeyManagement(session).sendHPB();
            configuration.getLogger().info(
                Messages.getString("hpb.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
        } catch (Exception e) {
            configuration.getLogger().error(
                Messages.getString("hpb.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
            throw e;
        }
    }

    /**
     * Sends the SPR order to the bank.
     *
     * @param user
     *            the user ID
     * @param product
     *            the session product
     * @throws Exception
     */
    public void revokeSubscriber(User user, Product product) throws Exception {
        String userId = user.getUserId();

        configuration.getLogger().info(
            Messages.getString("spr.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

        EbicsSession session = createSession(user, product);

        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            getKeyManagement(session).lockAccess();
        } catch (Exception e) {
            configuration.getLogger().error(
                Messages.getString("spr.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
            throw e;
        }

        configuration.getLogger().info(
            Messages.getString("spr.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
    }

    /**
     * Sends a file to the EBICS bank server
     * @throws Exception
     */
    public void sendFile(File file, User user, Product product, EbicsUploadOrder uploadOrder) throws Exception {
        EbicsSession session = createSession(user, product);

        final FileTransfer transferManager = getFileTransfer(session);

        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            transferManager.sendFile(IOUtils.getFileContent(file), uploadOrder);
        } catch (IOException | EbicsException e) {
            configuration.getLogger().error(
                Messages.getString("upload.file.error", Constants.APPLICATION_BUNDLE_NAME,
                    file.getAbsolutePath()), e);
            throw e;
        }
    }

    private KeyManagement getKeyManagement(EbicsSession session) {
        final KeyManagement keyManagement;
        switch (session.getUser().getEbicsVersion()) {
            case H003:
                keyManagement = new org.ebics.client.keymgmt.h003.KeyManagementImpl(session);
                break;
            case H004:
                keyManagement = new org.ebics.client.keymgmt.h004.KeyManagementImpl(session);
                break;
            default: //H005
                keyManagement = new org.ebics.client.keymgmt.h005.KeyManagementImpl(session);
                break;
        }
        return keyManagement;
    }

    private FileTransfer getFileTransfer(EbicsSession session) {
        final FileTransfer transferManager;
        switch (session.getUser().getEbicsVersion()) {
            case H003:
                transferManager = new org.ebics.client.filetransfer.h003.FileTransferImpl(session);
                break;
            case H004:
                transferManager = new org.ebics.client.filetransfer.h004.FileTransferImpl(session);
                break;
            default: //H005
                transferManager = new org.ebics.client.filetransfer.h005.FileTransferImpl(session);
                break;
        }
        return transferManager;
    }

    public void fetchFile(File file, User user, Product product, EbicsDownloadOrder downloadOrder,
        boolean isTest) throws IOException, EbicsException {
        final FileTransfer transferManager;
        EbicsSession session = createSession(user, product);
        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }
        transferManager = getFileTransfer(session);

        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            transferManager.fetchFile(downloadOrder, file);
        } catch (NoDownloadDataAvailableException e) {
            // don't log this exception as an error, caller can decide how to handle
            throw e;
        } catch (Exception e) {
            configuration.getLogger().error(
                Messages.getString("download.file.error", Constants.APPLICATION_BUNDLE_NAME), e);
            throw e;
        }
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    public void quit() {
        try {
            for (User user : users.values()) {
                if (user.needsSave()) {
                    configuration.getLogger().info(
                        Messages.getString("app.quit.users", Constants.APPLICATION_BUNDLE_NAME,
                            user.getUserId()));
                    configuration.getSerializationManager().serialize(user);
                }
            }

            for (Partner partner : partners.values()) {
                if (partner.needsSave()) {
                    configuration.getLogger().info(
                        Messages.getString("app.quit.partners", Constants.APPLICATION_BUNDLE_NAME,
                            partner.getPartnerId()));
                    configuration.getSerializationManager().serialize(partner);
                }
            }

            for (Bank bank : banks.values()) {
                if (bank.needsSave()) {
                    configuration.getLogger().info(
                        Messages.getString("app.quit.banks", Constants.APPLICATION_BUNDLE_NAME,
                            bank.getHostId()));
                    configuration.getSerializationManager().serialize(bank);
                }
            }
        } catch (EbicsException e) {
            configuration.getLogger().info(
                Messages.getString("app.quit.error", Constants.APPLICATION_BUNDLE_NAME));
        }

        clearTraces();
    }

    public void clearTraces() {
        configuration.getLogger().info(
            Messages.getString("app.cache.clear", Constants.APPLICATION_BUNDLE_NAME));
        configuration.getTraceManager().clear();
    }

    public static class ConfigProperties {
        Properties properties = new Properties();

        public ConfigProperties(File file) throws FileNotFoundException, IOException {
            properties.load(new FileInputStream(file));
        }

        public String get(String key) {
            String value = properties.getProperty(key);
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("property not set or empty: " + key);
            }
            return value.trim();
        }
    }

    private User createUser(ConfigProperties properties, PasswordCallback pwdHandler)
        throws Exception {
        EbicsVersion ebicsVersion = EbicsVersion.valueOf(properties.get("ebicsVersion"));
        String userId = properties.get("userId");
        String partnerId = properties.get("partnerId");
        String bankUrl = properties.get("bank.url");
        String bankName = properties.get("bank.name");
        String hostId = properties.get("hostId");
        String userName = properties.get("user.name");
        String userEmail = properties.get("user.email");
        String userCountry = properties.get("user.country");
        String userOrg = properties.get("user.org");
        final boolean useCertificates;
        if (ebicsVersion == EbicsVersion.H005)
            //Due to missing h005.PubKeyInfoType.getPubKeyValue in EBCICS XSDs is not possible to calculate exp + module, must be x509 cert instead
            useCertificates = true;
        else
            useCertificates = false;
        boolean saveCertificates = true;
        return createUser(new URL(bankUrl), ebicsVersion, bankName, hostId, partnerId, userId, userName, userEmail,
            userCountry, userOrg, useCertificates, saveCertificates, pwdHandler);
    }

    private static CommandLine parseArguments(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        options.addOption(null, "help", false, "Print this help text");
        CommandLine line = parser.parse(options, args);
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            System.out.println();
            formatter.printHelp(EbicsClient.class.getSimpleName(), options);
            System.out.println();
            System.exit(0);
        }
        return line;
    }

    public static EbicsClient createEbicsClient(File rootDir, File configFile) throws FileNotFoundException,
        IOException {
        ConfigProperties properties = new ConfigProperties(configFile);
        final String country = properties.get("countryCode").toUpperCase();
        final String language = properties.get("languageCode").toLowerCase();
        final String productName = properties.get("productName");

        final Locale locale = new Locale(language, country);

        DefaultConfiguration configuration = new DefaultConfiguration(rootDir.getAbsolutePath()) {

            @Override
            public Locale getLocale() {
                return locale;
            }
        };

        EbicsClient client = new EbicsClient(configuration, properties);

        Product product = new Product(productName, language, null);

        client.setDefaultProduct(product);

        return client;
    }

    public void createDefaultUser() throws Exception {
        defaultUser = createUser(properties, createPasswordCallback());
    }

    public void loadDefaultUser() throws Exception {
        String userId = properties.get("userId");
        String hostId = properties.get("hostId");
        String partnerId = properties.get("partnerId");
        defaultUser = loadUser(hostId, partnerId, userId, createPasswordCallback());
    }

    private PasswordCallback createPasswordCallback() {
        final String password = properties.get("password");
        PasswordCallback pwdHandler = new PasswordCallback() {

            @Override
            public char[] getPassword() {
                return password.toCharArray();
            }
        };
        return pwdHandler;
    }

    private void setDefaultProduct(Product product) {
        this.defaultProduct = product;
    }

    public User getDefaultUser() {
        return defaultUser;
    }

    private static void addOption(Options options, EbicsOrderType type, String description) {
        options.addOption(null, type.name().toLowerCase(), false, description);
    }

    private static boolean hasOption(CommandLine cmd, EbicsOrderType type) {
        return cmd.hasOption(type.name().toLowerCase());
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        addOption(options, EbicsOrderType.INI, "Send INI request");
        addOption(options, EbicsOrderType.HIA, "Send HIA request");
        addOption(options, EbicsOrderType.HPB, "Send HPB request");
        options.addOption(null, "letters", false, "Create INI Letters");
        options.addOption(null, "create", false, "Create and initialize EBICS user");
        addOption(options, EbicsOrderType.STA,"Fetch STA file (MT940 file)");
        addOption(options, EbicsOrderType.VMK, "Fetch VMK file (MT942 file)");
        addOption(options, EbicsOrderType.ZDF, "Fetch ZDF file (zip file with documents)");
        addOption(options, EbicsOrderType.ZB6, "Fetch ZB6 file");
        addOption(options, EbicsOrderType.PTK, "Fetch client protocol file (TXT)");
        addOption(options, EbicsOrderType.HAC, "Fetch client protocol file (XML)");
        addOption(options, EbicsOrderType.Z01, "Fetch Z01 file");

        addOption(options, EbicsOrderType.XKD, "Send payment order file (DTA format)");
        addOption(options, EbicsOrderType.FUL, "Send payment order file (any format)");
        addOption(options, EbicsOrderType.XCT, "Send XCT file (any format)");
        addOption(options, EbicsOrderType.XE2, "Send XE2 file (any format)");
        addOption(options, EbicsOrderType.CCT, "Send CCT file (any format)");

        options.addOption(null, "skip_order", true, "Skip a number of order ids");

        options.addOption("o", "output", true, "output file");
        options.addOption("i", "input", true, "input file");

        options.addOption("p", "params", true, "key:value array of string parameters for upload or download request, example FORMAT:pain.001 TEST:TRUE EBCDIC:TRUE");
        options.addOption("sn","service-name", true, "EBICS 3.0 Name of service, example 'SCT' (SEPA credit transfer)");
        options.addOption("so","service-option", true, "EBICS 3.0 Optional characteristic(s) of a service Example: “URG” = urgent");
        options.addOption("ss", "service-scope", true, "EBICS 3.0 Specifies whose rules have to be taken into account for the service. 2-char country codes, 3-char codes for other scopes “BIL” means bilaterally agreed");
        options.addOption("ct", "service-container", false, "EBICS 3.0 Flag to indicate the use of a container");
        options.addOption("mn", "service-message-name", true, "EBICS 3.0 Service message name, for example pain.001 or mt103");
        options.addOption("mve", "service-message-version", true, "EBICS 3.0 Service message version for ISO formats, for example 03");
        options.addOption("mva", "service-message-variant", true, "EBICS 3.0 Service message variant for ISO formats, for example 001");
        options.addOption("mf", "service-message-format", true, "EBICS 3.0 Service message format, for example XML, JSON, PFD, ASN1");

        CommandLine cmd = parseArguments(options, args);

        File defaultRootDir = new File(System.getProperty("user.home") + File.separator + "ebics"
            + File.separator + "client");
        File ebicsClientProperties = new File(defaultRootDir, "ebics.txt");
        EbicsClient client = createEbicsClient(defaultRootDir, ebicsClientProperties);

        if (cmd.hasOption("create")) {
            client.createDefaultUser();
        } else {
            client.loadDefaultUser();
        }

        if (cmd.hasOption("letters")) {
            client.createLetters(client.defaultUser, false);
        }

        if (hasOption(cmd, EbicsOrderType.INI)) {
            client.sendINIRequest(client.defaultUser, client.defaultProduct);
        }
        if (hasOption(cmd, EbicsOrderType.HIA)) {
            client.sendHIARequest(client.defaultUser, client.defaultProduct);
        }
        if (hasOption(cmd, EbicsOrderType.HPB)) {
            client.sendHPBRequest(client.defaultUser, client.defaultProduct);
        }

        //Download file
        String outputFileValue = cmd.getOptionValue("o");
        if (outputFileValue != null)
            fetchFile(cmd, client, outputFileValue);

        //Upload file
        String inputFileValue = cmd.getOptionValue("i");
        if (inputFileValue != null)
            sendFile(cmd, client, inputFileValue);

        if (cmd.hasOption("skip_order")) {
            int count = Integer.parseInt(cmd.getOptionValue("skip_order"));
            while(count-- > 0) {
                client.defaultUser.getPartner().nextOrderId();
            }
        }

        client.quit();
    }

    private static void fetchFile(CommandLine cmd, EbicsClient client, String outputFileValue) throws IOException, EbicsException {
        final EbicsDownloadOrder downloadOrder;
        final Map<String, String> params = readParams(cmd.getOptionValues("p"));
        //TBD: Reading of start/date from command line
        final Date start = null;
        final Date end = null;
        switch (client.defaultUser.getEbicsVersion()) {
            case H003:
            case H004:
                downloadOrder = new EbicsDownloadOrder(readOrderType(cmd, Arrays.asList(EbicsOrderType.STA, EbicsOrderType.VMK,
                        EbicsOrderType.ZDF, EbicsOrderType.ZB6, EbicsOrderType.PTK, EbicsOrderType.HAC, EbicsOrderType.Z01)), start, end, params);
                break;
            default:
                downloadOrder = new EbicsDownloadOrder(readEbicsService(cmd), start, end, params);
                break;
        }
        client.fetchFile(getOutputFile(outputFileValue), client.defaultUser,
            client.defaultProduct, downloadOrder, false);
    }

    private static EbicsService readEbicsService(CommandLine cmd) {
        return new EbicsService(cmd.getOptionValue("sn"), cmd.getOptionValue("so"), cmd.getOptionValue("ss"), cmd.getOptionValue("ct"),
                cmd.getOptionValue("mn"), cmd.getOptionValue("mve"), cmd.getOptionValue("mvr"), cmd.getOptionValue("mf"));
    }

    private static EbicsOrderType readOrderType(CommandLine cmd, List<EbicsOrderType> ebicsOrderTypes) {
        for (EbicsOrderType orderType : ebicsOrderTypes) {
            if (hasOption(cmd, orderType)) {
                return orderType;
            }
        }
        throw new IllegalArgumentException("For option o must be download ordertype specified for example -STA");
    }

    private static void sendFile(CommandLine cmd, EbicsClient client, String inputFileValue) throws Exception {
        final EbicsUploadOrder uploadOrder;
        final File inputFile = new File(inputFileValue);
        final Map<String, String> params = readParams(cmd.getOptionValues("p"));
        switch (client.defaultUser.getEbicsVersion()) {
            case H003:
            case H004:
                uploadOrder = new EbicsUploadOrder(
                        readOrderType(cmd, Arrays.asList(EbicsOrderType.XKD, EbicsOrderType.FUL, EbicsOrderType.XCT,
                        EbicsOrderType.XE2, EbicsOrderType.CCT)), !cmd.hasOption("ns"), params);
                break;
            default:
                uploadOrder = new EbicsUploadOrder(readEbicsService(cmd), !cmd.hasOption("ns"), inputFile.getName(), params);
                break;
        }
        client.sendFile(new File(inputFileValue), client.defaultUser,
            client.defaultProduct, uploadOrder);
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
