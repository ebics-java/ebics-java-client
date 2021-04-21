package org.ebics.client.console;

import org.ebics.client.api.EbicsModel;
import org.ebics.client.api.EbicsVersion;
import org.ebics.client.api.User;
import org.ebics.client.interfaces.Configuration;
import org.ebics.client.interfaces.PasswordCallback;
import org.ebics.client.session.DefaultConfiguration;
import org.ebics.client.session.Product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

/**
 * Ebics client app
 * This is the base part of Console App without being depended on specific EBICS h00X API.
 */
public class ConsoleAppBase {

    public static String	CONSOLE_APP_BUNDLE_NAME		= "org.ebics.client.console.messages";

    private User defaultUser;
    private final ConfigProperties properties;
    private Product defaultProduct;
    private EbicsModel ebicsModel;
    private Configuration configuration;

    /**
     * Constructs a new ebics client application
     *
     * @param properties     the property file of default user/partner/bank parameters
     * @param configuration  the application configuration
     * @param defaultProduct the default EBICS product
     */
    protected ConsoleAppBase(Configuration configuration, ConfigProperties properties, Product defaultProduct) {
        this.configuration = configuration;
        this.ebicsModel = new EbicsModel(configuration);
        this.properties = properties;
        this.defaultProduct = defaultProduct;
    }

    public static ConsoleAppBase createConsoleApp(File rootDir, File defaultEbicsConfigFile) throws FileNotFoundException,
            IOException {
        ConfigProperties properties = new ConfigProperties(defaultEbicsConfigFile);

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

        Product product = new Product(productName, language, null);

        return new ConsoleAppBase(configuration, properties, product);
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
        return ebicsModel.createUser(new URL(bankUrl), ebicsVersion, bankName, hostId, partnerId, userId, userName, userEmail,
                userCountry, userOrg, useCertificates, true, pwdHandler);
    }

    public User getDefaultUser() {
        return defaultUser;
    }

    public void createDefaultUser() throws Exception {
        defaultUser = createUser(properties, createPasswordCallback());
    }

    public void loadDefaultUser() throws Exception {
        String userId = properties.get("userId");
        String hostId = properties.get("hostId");
        String partnerId = properties.get("partnerId");
        defaultUser = ebicsModel.loadUser(hostId, partnerId, userId, createPasswordCallback());
    }

    private PasswordCallback createPasswordCallback() {
        final String password = properties.get("password");
        return password::toCharArray;
    }

    public Product getDefaultProduct() {
        return defaultProduct;
    }

    public EbicsModel getEbicsModel() {
        return ebicsModel;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
