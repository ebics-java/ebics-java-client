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

package org.ebics.client.api.h003;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ebics.client.api.Bank;
import org.ebics.client.api.EbicsVersion;
import org.ebics.client.api.Partner;
import org.ebics.client.api.User;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.exception.NoDownloadDataAvailableException;
import org.ebics.client.filetransfer.h003.FileTransfer;
import org.ebics.client.interfaces.*;
import org.ebics.client.keymgmt.KeyManagement;
import org.ebics.client.messages.Messages;
import org.ebics.client.order.h003.EbicsDownloadOrder;
import org.ebics.client.order.h003.EbicsUploadOrder;
import org.ebics.client.session.DefaultConfiguration;
import org.ebics.client.session.Product;
import org.ebics.client.io.IOUtils;
import org.ebics.client.session.EbicsSession;
import org.ebics.client.utils.Constants;

/**
 * The ebics client application. Performs necessary tasks to contact the ebics
 * bank server like sending the INI, HIA and HPB requests for keys retrieval and
 * also performs the files transfer including uploads and downloads.
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
     * @param configuration the application configuration
     * @param properties
     */
    protected EbicsClient(Configuration configuration, ConfigProperties properties) {
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
     * @param user the concerned user
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
     * @param url            the bank URL
     * @param url            the bank name
     * @param hostId         the bank host ID
     * @param useCertificate does the bank use certificates ?
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
     * @param bank      the bank
     * @param partnerId the partner ID
     */
    private Partner createPartner(EbicsBank bank, String partnerId) {
        Partner partner = new Partner(bank, partnerId);
        partners.put(partnerId, partner);
        return partner;
    }

    /**
     * Creates a new ebics user and generates its certificates.
     *
     * @param url              the bank url
     * @param bankName         the bank name
     * @param hostId           the bank host ID
     * @param partnerId        the partner ID
     * @param userId           UserId as obtained from the bank.
     * @param name             the user name,
     * @param email            the user email
     * @param country          the user country
     * @param organization     the user organization or company
     * @param useCertificates  does the bank use certificates ?
     * @param saveCertificates save generated certificates?
     * @param passwordCallback a callback-handler that supplies us with the password. This
     *                         parameter can be null, in this case no password is used.
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

    public void createLetters(EbicsUser user, boolean useCertificates)
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
     * @param user    the user ID
     * @param product the application product
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
     * @param user    the user ID.
     * @param product the application product.
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
     * @param user    the user ID
     * @param product the session product
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
     *
     * @throws Exception
     */
    public void sendFile(File file, User user, Product product, EbicsUploadOrder uploadOrder) throws Exception {
        EbicsSession session = createSession(user, product);

        final FileTransfer transferManager = new FileTransfer(session);

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


    public void fetchFile(File file, User user, Product product, EbicsDownloadOrder downloadOrder,
                          boolean isTest) throws IOException, EbicsException {

        EbicsSession session = createSession(user, product);
        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }
        final FileTransfer transferManager = new FileTransfer(session);

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
            //Due to missing h005.PubKeyInfoType.getPubKeyValue in EBICS XSDs is not possible to calculate exp + module,
            //x509 certificates must be used instead
            useCertificates = true;
        else
            useCertificates = false;
        return createUser(new URL(bankUrl), ebicsVersion, bankName, hostId, partnerId, userId, userName, userEmail,
                userCountry, userOrg, useCertificates, true, pwdHandler);
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
        PasswordCallback pwdHandler = () -> password.toCharArray();
        return pwdHandler;
    }

    private void setDefaultProduct(Product product) {
        this.defaultProduct = product;
    }

    public User getDefaultUser() {
        return defaultUser;
    }

    public Product getDefaultProduct() {
        return defaultProduct;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
