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

package org.kopi.ebics.client;

import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.Security;
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
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.exception.NoDownloadDataAvailableException;
import org.kopi.ebics.interfaces.Configuration;
import org.kopi.ebics.interfaces.EbicsBank;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.interfaces.InitLetter;
import org.kopi.ebics.interfaces.LetterManager;
import org.kopi.ebics.interfaces.PasswordCallback;
import org.kopi.ebics.io.IOUtils;
import org.kopi.ebics.messages.Messages;
import org.kopi.ebics.schema.h003.OrderAttributeType;
import org.kopi.ebics.session.DefaultConfiguration;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.session.Product;
import org.kopi.ebics.utils.Constants;

import java.util.Scanner;

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
    private final Messages messages;
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
        this.messages = new Messages(Constants.APPLICATION_BUNDLE_NAME, configuration.getLocale());
        configuration.getLogger().info(messages.getString("init.configuration"));
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
            messages.getString("user.create.directories", user.getUserId()));
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
     * @param name
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
    public User createUser(URL url, String bankName, String hostId, String partnerId,
        String userId, String name, String email, String country, String organization,
        boolean useCertificates, boolean saveCertificates, boolean useHSM, PasswordCallback passwordCallback)
        throws Exception {
        configuration.getLogger().info(messages.getString("user.create.info", userId));

      //  System.out.println("certs:" + useCertificates);
        Bank bank = createBank(url, bankName, hostId, useCertificates);
        Partner partner = createPartner(bank, partnerId);
        try {
            User user = new User(partner, userId, name, email, country, organization, useHSM,
                passwordCallback);
            createUserDirectories(user);
            if (saveCertificates) {
                user.saveUserCertificates(configuration.getKeystoreDirectory(user));
            }
            configuration.getSerializationManager().serialize(bank);
            configuration.getSerializationManager().serialize(partner);
            configuration.getSerializationManager().serialize(user);
            if (!useHSM)
              createLetters(user, useCertificates);
            
            users.put(userId, user);
            partners.put(partner.getPartnerId(), partner);
            banks.put(bank.getHostId(), bank);

            configuration.getLogger().info(messages.getString("user.create.success", userId));
            return user;
        } catch (Exception e) {
            configuration.getLogger().error(messages.getString("user.create.error"), e);
            throw e;
        }
    }

    private void createLetters(EbicsUser user, boolean useCertificates)
        throws GeneralSecurityException, IOException, EbicsException {
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
    
    private void resetIniHia(User user)
            throws GeneralSecurityException, IOException, EbicsException {
    	
          user.setInitializedHIA(false);
          user.setInitialized(false);
          users.put(user.getUserId(), user);
        }
    

    /**
     * Loads a user knowing its ID
     *
     * @throws Exception
     */
    public User loadUser(String hostId, String partnerId, String userId, String userName, String path,
    	   String pkcs11ModPath, PasswordCallback passwordCallback) throws Exception {
        configuration.getLogger().info(messages.getString("user.load.info", userId));

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
        	    
            	user = new User(partner, userId, userName, input, passwordCallback);
            	if (path.equals("default"))
            		path = configuration.getKeystoreDirectory(user) ;
            	
            	if (path.equals("use-hsm")) {
            		user.setisUsingHSM(true);         		
            		String pkcs11Config = pkcs11ModPath;
            	    ByteArrayInputStream confStream = new ByteArrayInputStream(pkcs11Config.getBytes());
            	    Provider prov = new sun.security.pkcs11.SunPKCS11(confStream);
            	    Security.addProvider(prov);
            	    
            	    user.setProvider(prov);
            	}
            	
            	user.loadCertificates(path);

            }
            

            users.put(userId, user);
            partners.put(partner.getPartnerId(), partner);
            banks.put(bank.getHostId(), bank);
            configuration.getLogger().info(messages.getString("user.load.success", userId));
            return user;
        } catch (Exception e) {
            configuration.getLogger().error(messages.getString("user.load.error"), e);
            throw e;
        }
    }

    /**
     * Sends an INI request to the ebics bank server
     *
     * @param user the user
     * @param product the application product
     * @throws Exception
     */
    public void sendINIRequest(User user, Product product) throws Exception {
        String userId = user.getUserId();
        configuration.getLogger().info(messages.getString("ini.request.send", userId));
        if (user.isInitialized()) {
            configuration.getLogger().info(messages.getString("user.already.initialized", userId));
            return;
        }
        EbicsSession session = createSession(user, product);
        KeyManagement keyManager = new KeyManagement(session);
        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));
        try {
            keyManager.sendINI(null);
            user.setInitialized(true);
            configuration.getLogger().info(messages.getString("ini.send.success", userId));
        } catch (Exception e) {
            configuration.getLogger().error(messages.getString("ini.send.error", userId), e);
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
        configuration.getLogger().info(messages.getString("hia.request.send", userId));
        if (user.isInitializedHIA()) {
            configuration.getLogger()
                .info(messages.getString("user.already.hia.initialized", userId));
            return;
        }
        EbicsSession session = createSession(user, product);
        KeyManagement keyManager = new KeyManagement(session);
        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));
        try {
            keyManager.sendHIA(null);
            user.setInitializedHIA(true);
        } catch (Exception e) {
            configuration.getLogger().error(messages.getString("hia.send.error", userId), e);
            throw e;
        }
        configuration.getLogger().info(messages.getString("hia.send.success", userId));
    }

    /**
     * Sends a HPB request to the ebics server.
     */
    public void sendHPBRequest(User user, Product product) throws Exception {
        String userId = user.getUserId();
        configuration.getLogger().info(messages.getString("hpb.request.send", userId));

        EbicsSession session = createSession(user, product);
        KeyManagement keyManager = new KeyManagement(session);

        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            keyManager.sendHPB();
            configuration.getLogger().info(messages.getString("hpb.send.success", userId));
        } catch (Exception e) {
            configuration.getLogger().error(messages.getString("hpb.send.error", userId), e);
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

        configuration.getLogger().info(messages.getString("spr.request.send", userId));

        EbicsSession session = createSession(user, product);
        KeyManagement keyManager = new KeyManagement(session);

        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            keyManager.lockAccess();
        } catch (Exception e) {
            configuration.getLogger().error(messages.getString("spr.send.error", userId), e);
            throw e;
        }

        configuration.getLogger().info(messages.getString("spr.send.success", userId));
    }

    /**
     * Sends a file to the ebics bank server
     * @throws Exception
     */
    public void sendFile(File file, User user, Product product, OrderType orderType) throws Exception {
        EbicsSession session = createSession(user, product);
        OrderAttributeType.Enum orderAttribute = OrderAttributeType.OZHNN;

        FileTransfer transferManager = new FileTransfer(session);

        if (orderType==OrderType.XE2) {
        	session.addSessionParam("FORMAT", "pain.001.001.03.ch.02");
        }
        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            transferManager.sendFile(IOUtils.getFileContent(file), orderType, orderAttribute);
        } catch (IOException | EbicsException e) {
            configuration.getLogger()
                .error(messages.getString("upload.file.error", file.getAbsolutePath()), e);
            throw e;
        }
    }

    public void sendFile(byte[] data, User user, Product product, OrderType orderType) throws Exception {
        EbicsSession session = createSession(user, product);
        OrderAttributeType.Enum orderAttribute = OrderAttributeType.OZHNN;

        FileTransfer transferManager = new FileTransfer(session);

        if (orderType==OrderType.XE2) {
               session.addSessionParam("FORMAT", "pain.001.001.03.ch.02");
        }
        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            transferManager.sendFile(data, orderType, orderAttribute);
        } catch (IOException | EbicsException e) {
            configuration.getLogger()
            .error(messages.getString("upload.file.error"), e);
        throw e;
        }
    }

    
    public void sendFile(File file, OrderType orderType) throws Exception {
        sendFile(file, defaultUser, defaultProduct, orderType);
    }
    
    public void sendFile(byte[] data, OrderType orderType) throws Exception {
         sendFile(data, defaultUser, defaultProduct, orderType);
   }


    public void fetchFile(File file, User user, Product product, OrderType orderType,
        boolean isTest, Date start, Date end) throws IOException, EbicsException {
        FileTransfer transferManager;
        EbicsSession session = createSession(user, product);
        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }
        transferManager = new FileTransfer(session);

        configuration.getTraceManager().setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            transferManager.fetchFile(orderType, start, end, file);
        } catch (NoDownloadDataAvailableException e) {
            // don't log this exception as an error, caller can decide how to handle
            throw e;
        } catch (Exception e) {
            configuration.getLogger().error(messages.getString("download.file.error"), e);
            throw e;
        }
    }

    public void fetchFile(File file, OrderType orderType, Date start, Date end) throws IOException,
        EbicsException {
        fetchFile(file, defaultUser, defaultProduct, orderType, false, start, end);
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    public void quit() {
        try {
            for (User user : users.values()) {
                if (user.needsSave()) {
                    configuration.getLogger()
                        .info(messages.getString("app.quit.users", user.getUserId()));
                    configuration.getSerializationManager().serialize(user);
                }
            }

            for (Partner partner : partners.values()) {
                if (partner.needsSave()) {
                    configuration.getLogger()
                        .info(messages.getString("app.quit.partners", partner.getPartnerId()));
                    configuration.getSerializationManager().serialize(partner);
                }
            }

            for (Bank bank : banks.values()) {
                if (bank.needsSave()) {
                    configuration.getLogger()
                        .info(messages.getString("app.quit.banks", bank.getHostId()));
                    configuration.getSerializationManager().serialize(bank);
                }
            }
        } catch (EbicsException e) {
            configuration.getLogger().info(messages.getString("app.quit.error"));
        }

        clearTraces();
    }

    public void clearTraces() {
        configuration.getLogger().info(messages.getString("app.cache.clear"));
        configuration.getTraceManager().clear();
    }

    public static class ConfigProperties {
        Properties properties = new Properties();

        public ConfigProperties(File file) throws IOException {
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

    private User createUser(ConfigProperties properties)
        throws Exception {
    	
    	boolean useHSM = false;
        boolean useCertificates = false;
        boolean saveCertificates = true;
        PasswordCallback passwordCallback = null;
        String userId = properties.get("userId");
        String partnerId = properties.get("partnerId");
        String bankUrl = properties.get("bank.url");
        String bankName = properties.get("bank.name");
        String hostId = properties.get("hostId");
        String userName = properties.get("user.name");
        String userEmail = properties.get("user.email");
        String userCountry = properties.get("user.country");
        String userOrg = properties.get("user.org");
        String path = properties.get("keyDir");  
        if (path.equals("use-hsm")) {
        	useHSM = true;
        	saveCertificates = false;
        }       
        else {
        	passwordCallback = createPasswordCallback();
        }
        return createUser(new URL(bankUrl), bankName, hostId, partnerId, userId, userName, userEmail,
            userCountry, userOrg, useCertificates, saveCertificates, useHSM, passwordCallback);
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

    public static EbicsClient createEbicsClient(File rootDir, File configFile) throws IOException {
        ConfigProperties properties = new ConfigProperties(configFile);
        final String country = properties.get("countryCode").toUpperCase();
        final String language = properties.get("languageCode").toLowerCase();
        final String productName = properties.get("productName");

        final Locale locale = new Locale(language, country);

        DefaultConfiguration configuration = new DefaultConfiguration(rootDir.getAbsolutePath(),
            properties.properties) {

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
        defaultUser = createUser(properties);
    }

    public void loadDefaultUser() throws Exception {
    	String pkcs11ModPath = null;
        String userId = properties.get("userId");
        String hostId = properties.get("hostId");
        String partnerId = properties.get("partnerId");
        String userName = properties.get("user.name");
        String path = properties.get("keyDir"); 
        if (path.equals("use-hsm"))
          pkcs11ModPath = properties.get("pkcs11.module.path"); 
        
        defaultUser = loadUser(hostId, partnerId, userId, userName, path, pkcs11ModPath, createPasswordCallback());
    }

    private PasswordCallback createPasswordCallback() {
        Console cnsl = System.console(); 
        if (cnsl == null) { 
            System.out.println("No console available"); 
          } 
  
        // Read password 
        final char[] password = cnsl.readPassword("Enter password : "); 
         
        return new PasswordCallback() {

            @Override
            public char[] getPassword() {
                return password;
            }
        };
    }

    private void setDefaultProduct(Product product) {
        this.defaultProduct = product;
    }

    public User getDefaultUser() {
        return defaultUser;
    }

    private static void addOption(Options options, OrderType type, String description) {
        options.addOption(null, type.name().toLowerCase(), false, description);
    }

    private static boolean hasOption(CommandLine cmd, OrderType type) {
        return cmd.hasOption(type.name().toLowerCase());
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        addOption(options, OrderType.INI, "Send INI request");
        addOption(options, OrderType.HIA, "Send HIA request");
        addOption(options, OrderType.HPB, "Send HPB request");
        options.addOption(null, "letters", false, "Create INI Letters");
        options.addOption(null, "create", false, "Create and initialize EBICS user");
        options.addOption(null, "reset-init", false, "Reset INI and HIA to not initialized (only locally)");
        addOption(options, OrderType.STA,"Fetch STA file (MT940 file)");
        addOption(options, OrderType.VMK, "Fetch VMK file (MT942 file)");
        addOption(options, OrderType.C52, "Fetch camt.052 file");
        addOption(options, OrderType.C53, "Fetch camt.053 file");
        addOption(options, OrderType.C54, "Fetch camt.054 file");
        addOption(options, OrderType.ZDF, "Fetch ZDF file (zip file with documents)");
        addOption(options, OrderType.ZB6, "Fetch ZB6 file");
        addOption(options, OrderType.PTK, "Fetch client protocol file (TXT)");
        addOption(options, OrderType.HAC, "Fetch client protocol file (XML)");
        addOption(options, OrderType.Z01, "Fetch Z01 file");

        addOption(options, OrderType.XKD, "Send payment order file (DTA format)");
        addOption(options, OrderType.FUL, "Send payment order file (any format)");
        addOption(options, OrderType.XCT, "Send XCT file (any format)");
        addOption(options, OrderType.XE2, "Send XE2 file (any format)");
        addOption(options, OrderType.CCT, "Send CCT file (any format)");
        
        addOption(options, OrderType.Z53, "camt.053");
        addOption(options, OrderType.Z54, "camt.054");

        options.addOption(null, "skip_order", true, "Skip a number of order ids");

        options.addOption("o", "output", true, "output file");
        options.addOption("i", "input", true, "input file");


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
        
        if (cmd.hasOption("reset-init")) {
            client.resetIniHia(client.defaultUser);
        }

        if (hasOption(cmd, OrderType.INI)) {
            client.sendINIRequest(client.defaultUser, client.defaultProduct);
        }
        if (hasOption(cmd, OrderType.HIA)) {
            client.sendHIARequest(client.defaultUser, client.defaultProduct);
        }
        if (hasOption(cmd, OrderType.HPB)) {
            client.sendHPBRequest(client.defaultUser, client.defaultProduct);
        }

        String outputFileValue = cmd.getOptionValue("o");
        String inputFileValue = cmd.getOptionValue("i");

        List<OrderType> fetchFileOrders = Arrays.asList(OrderType.STA, OrderType.VMK,
            OrderType.C52, OrderType.C53, OrderType.C54,
            OrderType.ZDF, OrderType.ZB6, OrderType.PTK, OrderType.HAC, OrderType.Z01,
            OrderType.Z53, OrderType.Z54);

        for (OrderType type : fetchFileOrders) {
            if (hasOption(cmd, type)) {
                client.fetchFile(getOutputFile(outputFileValue), client.defaultUser,
                    client.defaultProduct, type, false, null, null);
                break;
            }
        }

        List<OrderType> sendFileOrders = Arrays.asList(OrderType.XKD, OrderType.FUL, OrderType.XCT,
            OrderType.XE2, OrderType.CCT);
        for (OrderType type : sendFileOrders) {
            if (hasOption(cmd, type)) {
                client.sendFile(new File(inputFileValue), client.defaultUser,
                    client.defaultProduct, type);
                break;
            }
        }

        if (cmd.hasOption("skip_order")) {
            int count = Integer.parseInt(cmd.getOptionValue("skip_order"));
            while(count-- > 0) {
                client.defaultUser.getPartner().nextOrderId();
            }
        }
        client.quit();
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
