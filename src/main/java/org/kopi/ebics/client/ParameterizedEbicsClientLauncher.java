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
 */

package org.kopi.ebics.client;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import org.kopi.ebics.interfaces.EbicsBank;
import org.kopi.ebics.interfaces.EbicsPartner;
import org.kopi.ebics.interfaces.PasswordCallback;
import org.kopi.ebics.session.DefaultConfiguration;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.session.Product;

/**
 * Parameter-based launcher that avoids relying on a persisted ebics.txt file in the workspace.
 * It receives runtime parameters from environment variables.
 */
public final class ParameterizedEbicsClientLauncher {
    private static final Set<String> RESERVED_FLAGS = Set.of(
        "--create",
        "--ini",
        "--hia",
        "--hpb",
        "--help"
    );

    private ParameterizedEbicsClientLauncher() {
    }

    public static void main(String[] args) throws Exception {
        ParsedArguments parsedArguments = ParsedArguments.parse(args);
        if (parsedArguments.hasFlag("--help")) {
            printUsage();
            return;
        }

        String passphrase = requiredEnv("EBICS_PASSWORD");
        String userId = requiredEnv("EBICS_USER_ID");
        String partnerId = requiredEnv("EBICS_PARTNER_ID");
        String hostId = requiredEnv("EBICS_HOST_ID");
        String bankUrl = requiredEnv("EBICS_BANK_URL");
        String languageCode = env("EBICS_LANGUAGE_CODE", "de");
        String countryCode = env("EBICS_COUNTRY_CODE", "DE").toUpperCase(Locale.ROOT);

        propagateOptionalSystemProperty(
            "ebics.key.length",
            normalize(System.getenv("EBICS_KEY_LENGTH"))
        );
        propagateOptionalSystemProperty(
            "ebics.cert.validity.years",
            normalize(System.getenv("EBICS_CERT_VALIDITY_YEARS"))
        );

        Properties properties = buildConfigurationProperties(languageCode, countryCode);
        File rootDirectory = rootDirectory();
        DefaultConfiguration configuration = createConfiguration(
            rootDirectory,
            properties,
            languageCode,
            countryCode
        );
        EbicsClient client = new EbicsClient(configuration, null);
        Product product = new Product(
            env("EBICS_PRODUCT_NAME", "EBICS Java Client"),
            languageCode,
            null
        );
        PasswordCallback passwordCallback = () -> passphrase.toCharArray();

        User user;
        if (parsedArguments.hasFlag("--create")) {
            user = client.createUser(
                new URL(bankUrl),
                env("EBICS_BANK_NAME", hostId),
                hostId,
                partnerId,
                userId,
                env("EBICS_USER_NAME", userId),
                env("EBICS_USER_EMAIL", userId + "@example.invalid"),
                env("EBICS_USER_COUNTRY", countryCode),
                env("EBICS_USER_ORGANIZATION", "EBICS"),
                resolveUseCertificate(),
                true,
                passwordCallback
            );
        } else {
            user = client.loadUser(hostId, partnerId, userId, passwordCallback);
            ensureLoadedUserMatchesConfiguredEndpoint(user, bankUrl, hostId);
        }

        if (parsedArguments.hasFlag("--ini")) {
            client.sendINIRequest(user, product);
        }
        if (parsedArguments.hasFlag("--hia")) {
            client.sendHIARequest(user, product);
        }
        if (parsedArguments.hasFlag("--hpb")) {
            client.sendHPBRequest(user, product);
        }

        String orderFlag = parsedArguments.firstOrderFlag();
        if (orderFlag != null) {
            OrderType orderType = OrderType.valueOf(orderFlag.substring(2).toUpperCase(Locale.ROOT));
            if (parsedArguments.inputPath() != null) {
                client.sendFile(
                    new File(parsedArguments.inputPath()),
                    user,
                    product,
                    orderType,
                    defaultUploadParams(user, orderType)
                );
            } else if (parsedArguments.outputPath() != null) {
                if (parsedArguments.startDate() != null || parsedArguments.endDate() != null) {
                    System.err.println(
                        "Date range arguments are ignored in parameterized mode for this order type."
                    );
                }
                client.fetchFile(
                    new File(parsedArguments.outputPath()),
                    user,
                    product,
                    orderType,
                    Boolean.parseBoolean(env("EBICS_TEST_MODE", "false"))
                );
            }
        }

        client.quit();
    }

    private static void printUsage() {
        String usage = "Usage: ParameterizedEbicsClientLauncher [--create] [--ini] [--hia] [--hpb]"
            + " [--<order>] [-i inputFile] [-o outputFile]\n"
            + "Required environment variables: EBICS_PASSWORD, EBICS_USER_ID, EBICS_PARTNER_ID,"
            + " EBICS_HOST_ID, EBICS_BANK_URL";
        System.out.println(usage);
    }

    private static EbicsUploadParams defaultUploadParams(User user, OrderType orderType) {
        if (orderType == OrderType.XE2) {
            var orderParams = new EbicsUploadParams.OrderParams(
                "MCT",
                "CH",
                null,
                "pain.001",
                "03",
                true
            );
            return new EbicsUploadParams(null, orderParams);
        }
        return new EbicsUploadParams(user.getPartner().nextOrderId(), null);
    }

    private static File rootDirectory() {
        String explicit = normalize(System.getenv("EBICS_ROOT_DIR"));
        if (explicit != null) {
            return new File(explicit);
        }
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            throw new IllegalStateException("Missing user.home for EBICS workspace resolution.");
        }
        return new File(new File(userHome), "ebics/client");
    }

    private static DefaultConfiguration createConfiguration(
        File rootDirectory,
        Properties properties,
        String languageCode,
        String countryCode
    ) {
        Locale locale = new Locale(
            languageCode.toLowerCase(Locale.ROOT),
            countryCode.toUpperCase(Locale.ROOT)
        );
        return new DefaultConfiguration(rootDirectory, properties) {
            @Override
            public Locale getLocale() {
                return locale;
            }
        };
    }

    private static Properties buildConfigurationProperties(
        String languageCode,
        String countryCode
    ) {
        Properties properties = new Properties();
        properties.setProperty("conf.file.name", "ebics.properties");
        properties.setProperty("keystore.dir.name", "keystore");
        properties.setProperty("traces.dir.name", "traces");
        properties.setProperty("serialization.dir.name", "serialized");
        properties.setProperty("ssltruststore.dir.name", "ssl");
        properties.setProperty("sslkeystore.dir.name", "ssl");
        properties.setProperty("sslbankcert.dir.name", "ssl");
        properties.setProperty("users.dir.name", "users");
        properties.setProperty("letters.dir.name", "letters");
        properties.setProperty("signature.version", env("EBICS_SIGNATURE_VERSION", "A005"));
        properties.setProperty("authentication.version", env("EBICS_AUTHENTICATION_VERSION", "X002"));
        properties.setProperty("encryption.version", env("EBICS_ENCRYPTION_VERSION", "E002"));
        properties.setProperty("ebics.version", env("EBICS_VERSION", "H003"));
        properties.setProperty("languageCode", languageCode);
        properties.setProperty("countryCode", countryCode);
        return properties;
    }

    private static boolean resolveUseCertificate() {
        String explicit = normalize(System.getenv("EBICS_USE_CERTIFICATE"));
        if (explicit != null) {
            return "true".equalsIgnoreCase(explicit);
        }
        String signatureVersion = env("EBICS_SIGNATURE_VERSION", "A005");
        return "A006".equalsIgnoreCase(signatureVersion);
    }

    private static void ensureLoadedUserMatchesConfiguredEndpoint(
        User user,
        String configuredBankUrl,
        String configuredHostId
    ) {
        if (user == null) {
            return;
        }

        EbicsPartner partner = user.getPartner();
        EbicsBank bank = partner == null ? null : partner.getBank();
        if (bank == null) {
            return;
        }

        String expectedUrl = normalize(configuredBankUrl);
        String loadedUrl = bank.getURL() == null ? null : normalize(bank.getURL().toString());
        if (expectedUrl != null && !expectedUrl.equals(loadedUrl)) {
            throw new IllegalStateException(
                "Loaded user endpoint does not match configured EBICS_BANK_URL. "
                    + "Run with --create or clean serialized state."
            );
        }

        String expectedHostId = normalize(configuredHostId);
        String loadedHostId = normalize(bank.getHostId());
        if (
            expectedHostId != null &&
            loadedHostId != null &&
            !expectedHostId.equals(loadedHostId)
        ) {
            throw new IllegalStateException(
                "Loaded user host id does not match configured EBICS_HOST_ID. "
                    + "Run with --create or clean serialized state."
            );
        }
    }

    private static void propagateOptionalSystemProperty(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        }
    }

    private static String requiredEnv(String key) {
        String value = normalize(System.getenv(key));
        if (value == null) {
            throw new IllegalArgumentException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static String env(String key, String fallback) {
        String value = normalize(System.getenv(key));
        return value == null ? fallback : value;
    }

    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static final class ParsedArguments {
        private final Set<String> flags = new LinkedHashSet<>();
        private final String inputPath;
        private final String outputPath;
        private final String startDate;
        private final String endDate;

        private ParsedArguments(
            Set<String> flags,
            String inputPath,
            String outputPath,
            String startDate,
            String endDate
        ) {
            this.flags.addAll(flags);
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        static ParsedArguments parse(String[] args) {
            Set<String> flags = new LinkedHashSet<>();
            String inputPath = null;
            String outputPath = null;
            String startDate = null;
            String endDate = null;
            if (args != null) {
                for (int index = 0; index < args.length; index++) {
                    String arg = args[index];
                    if (arg == null || arg.isBlank()) {
                        continue;
                    }
                    if ("-i".equals(arg) || "--input".equals(arg)) {
                        inputPath = requireValue(args, ++index, arg);
                        continue;
                    }
                    if ("-o".equals(arg) || "--output".equals(arg)) {
                        outputPath = requireValue(args, ++index, arg);
                        continue;
                    }
                    if ("-s".equals(arg) || "--start".equals(arg)) {
                        startDate = requireValue(args, ++index, arg);
                        continue;
                    }
                    if ("-e".equals(arg) || "--end".equals(arg)) {
                        endDate = requireValue(args, ++index, arg);
                        continue;
                    }
                    if (arg.startsWith("--")) {
                        flags.add(arg.toLowerCase(Locale.ROOT));
                    }
                }
            }
            return new ParsedArguments(flags, inputPath, outputPath, startDate, endDate);
        }

        private static String requireValue(String[] args, int index, String option) {
            if (args == null || index >= args.length) {
                throw new IllegalArgumentException("Missing value for option " + option);
            }
            String value = normalize(args[index]);
            if (value == null) {
                throw new IllegalArgumentException("Missing value for option " + option);
            }
            return value;
        }

        boolean hasFlag(String flag) {
            return flags.contains(flag.toLowerCase(Locale.ROOT));
        }

        String firstOrderFlag() {
            for (String flag : flags) {
                if (RESERVED_FLAGS.contains(flag)) {
                    continue;
                }
                String candidate = flag.startsWith("--")
                    ? flag.substring(2).toUpperCase(Locale.ROOT)
                    : flag.toUpperCase(Locale.ROOT);
                try {
                    OrderType.valueOf(candidate);
                    return flag;
                } catch (IllegalArgumentException ignored) {
                    // ignore unknown flags that are not EBICS order types
                }
            }
            return null;
        }

        String inputPath() {
            return inputPath;
        }

        String outputPath() {
            return outputPath;
        }

        String startDate() {
            return startDate;
        }

        String endDate() {
            return endDate;
        }
    }
}
