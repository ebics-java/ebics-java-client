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

package org.ebics.client.api;

import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.interfaces.*;
import org.ebics.client.messages.Messages;

import org.ebics.client.session.DefaultConfiguration;
import org.ebics.client.session.Product;
import org.ebics.client.io.IOUtils;
import org.ebics.client.session.EbicsSession;
import org.ebics.client.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ebics client application. Performs necessary tasks to contact the ebics
 * bank server like sending the INI, HIA and HPB requests for keys retrieval and
 * also performs the files transfer including uploads and downloads.
 */
public class EbicsModel {

    private static Logger logger = LoggerFactory.getLogger(EbicsModel.class);

    protected final Configuration configuration;
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Partner> partners = new HashMap<>();
    private final Map<String, Bank> banks = new HashMap<>();

    static {
        org.apache.xml.security.Init.init();
        java.security.Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Constructs a new ebics client application
     *
     * @param configuration the application configuration
     */
    public EbicsModel(Configuration configuration) {
        this.configuration = configuration;
        Messages.setLocale(configuration.getLocale());
        logger.info(
                Messages.getString("init.configuration", Constants.APPLICATION_BUNDLE_NAME));
        configuration.init();
    }

    public EbicsSession createSession(User user, Product product) {
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
        logger.info(
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
        logger.info(Messages.getString("user.create.info", Constants.APPLICATION_BUNDLE_NAME, userId));

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

            logger.info(
                    Messages.getString("user.create.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        } catch (Exception e) {
            logger.error(
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
        logger.info(
                Messages.getString("user.load.info", Constants.APPLICATION_BUNDLE_NAME, userId));

        try {
            Bank bank;
            Partner partner;
            User user;
            try (ObjectInputStream input = configuration.getSerializationManager().deserialize(
                    "bank-" + hostId)) {
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
            logger.info(
                    Messages.getString("user.load.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        } catch (Exception e) {
            logger.error(
                    Messages.getString("user.load.error", Constants.APPLICATION_BUNDLE_NAME), e);
            throw e;
        }
    }

    private List<String> listPersistentObjectId(final String prefix, final String extension) {
        String [] userFiles = new File(configuration.getSerializationDirectory()).list((dir, name) -> name.startsWith(prefix) && name.endsWith("." + extension));
        return Arrays.stream(userFiles).map(name -> name.replaceFirst(prefix, "").replaceFirst("\\." + extension, "")).collect(Collectors.toList());
    }

    public List<String> listUserId() {
        return listPersistentObjectId("user-", "cer");
    }

    public List<String> listPartnerId() {
        return listPersistentObjectId("partner-", "cer");
    }

    public List<String> listBankId() {
        return listPersistentObjectId("bank-", "cer");
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    public void saveAll() {
        try {
            for (User user : users.values()) {
                if (user.needsSave()) {
                    logger.info(
                            Messages.getString("user.save.info", Constants.APPLICATION_BUNDLE_NAME,
                                    user.getUserId()));
                    configuration.getSerializationManager().serialize(user);
                }
            }

            for (Partner partner : partners.values()) {
                if (partner.needsSave()) {
                    logger.info(
                            Messages.getString("partner.save.info", Constants.APPLICATION_BUNDLE_NAME,
                                    partner.getPartnerId()));
                    configuration.getSerializationManager().serialize(partner);
                }
            }

            for (Bank bank : banks.values()) {
                if (bank.needsSave()) {
                    logger.info(
                            Messages.getString("bank.save.info", Constants.APPLICATION_BUNDLE_NAME,
                                    bank.getHostId()));
                    configuration.getSerializationManager().serialize(bank);
                }
            }
        } catch (EbicsException e) {
            logger.info(
                    Messages.getString("app.quit.error", Constants.APPLICATION_BUNDLE_NAME));
        }

        clearTraces();
    }

    public void clearTraces() {
        logger.info(
                Messages.getString("app.cache.clear", Constants.APPLICATION_BUNDLE_NAME));
        configuration.getTraceManager().clear();
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
