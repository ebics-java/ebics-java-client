package org.ebics.client.api.h003;

import org.ebics.client.api.AbstractEbicsClient;
import org.ebics.client.api.User;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.exception.NoDownloadDataAvailableException;
import org.ebics.client.filetransfer.h003.FileTransfer;
import org.ebics.client.interfaces.Configuration;
import org.ebics.client.io.IOUtils;
import org.ebics.client.keymgmt.KeyManagement;
import org.ebics.client.messages.Messages;
import org.ebics.client.session.DefaultConfiguration;
import org.ebics.client.session.EbicsSession;
import org.ebics.client.session.Product;
import org.ebics.client.order.h003.EbicsDownloadOrder;
import org.ebics.client.order.h003.EbicsUploadOrder;
import org.ebics.client.keymgmt.h003.KeyManagementImpl;
import org.ebics.client.utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

public class EbicsClient extends AbstractEbicsClient {
    /**
     * Constructs a new ebics client application
     *
     * @param configuration the application configuration
     * @param properties
     */
    protected EbicsClient(Configuration configuration, ConfigProperties properties) {
        super(configuration, properties);
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

    private KeyManagement getKeyManagement(EbicsSession session) {
        return new KeyManagementImpl(session);
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
}
