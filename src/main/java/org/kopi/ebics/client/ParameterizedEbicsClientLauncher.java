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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.kopi.ebics.interfaces.EbicsBank;
import org.kopi.ebics.interfaces.EbicsOrderType;
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
        "--help",
        "--btd"
    );

    /**
     * EBICS 3.0 business transaction downloads always use the admin order type {@code BTD}; the
     * business order is carried by the service parameters instead of the 3-letter code.
     */
    private static final EbicsOrderType BTD_ORDER_TYPE = () -> "BTD";

    private ParameterizedEbicsClientLauncher() {
    }

    public static void main(String[] args) throws Exception {
        ParsedArguments parsedArguments = ParsedArguments.parse(args);
        if (parsedArguments.hasFlag("--help")) {
            printUsage();
            return;
        }

        // Every argument is checked before the first environment read, keystore access or bank
        // call. INI is one-shot at most banks: aborting on a missing --container after the INI
        // request has gone out would leave a half-initialised access behind.
        validateArguments(parsedArguments);

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

        if (parsedArguments.hasFlag("--btd")) {
            EbicsDownloadParams downloadParams = btdDownloadParams(parsedArguments);
            client.fetchFile(
                new File(requireOutputPath(parsedArguments)),
                user,
                product,
                BTD_ORDER_TYPE,
                downloadParams,
                Boolean.parseBoolean(env("EBICS_TEST_MODE", "false"))
            );
            client.quit();
            return;
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
                client.fetchFile(
                    new File(parsedArguments.outputPath()),
                    user,
                    product,
                    orderType,
                    legacyDownloadParams(parsedArguments),
                    Boolean.parseBoolean(env("EBICS_TEST_MODE", "false"))
                );
            }
        }

        client.quit();
    }

    private static void printUsage() {
        String usage = "Usage: ParameterizedEbicsClientLauncher [--create] [--ini] [--hia] [--hpb]"
            + " [--<order>] [-i inputFile] [-o outputFile] [-s start] [-e end]\n"
            + "EBICS 3.0 download: --btd --service <NAME> --scope <CC> --msg-name <name>"
            + " --msg-version <vv> --container <XML|ZIP|SVC>"
            + " [--option <OPT>] [-s YYYY-MM-DD] [-e YYYY-MM-DD] -o <file>\n"
            + "  e.g. --btd --service EOP --scope CH --msg-name camt.053 --msg-version 08"
            + " --container ZIP -o statement.zip\n"
            + "Required environment variables: EBICS_PASSWORD, EBICS_USER_ID, EBICS_PARTNER_ID,"
            + " EBICS_HOST_ID, EBICS_BANK_URL";
        System.out.println(usage);
    }

    /**
     * Rejects every unusable argument combination before the program talks to anyone. Nothing here
     * touches the network, the filesystem or the environment.
     */
    static void validateArguments(ParsedArguments parsedArguments) {
        if (parsedArguments.hasFlag("--btd")) {
            btdDownloadParams(parsedArguments);
            requireOutputPath(parsedArguments);
        } else {
            legacyDownloadParams(parsedArguments);
        }
    }

    /**
     * Builds the EBICS 3.0 service parameters for {@code --btd}. Fails fast on a missing mandatory
     * value, so a half-filled order is never sent to the bank. The date range pair itself is
     * checked by {@link EbicsDownloadParams}, which covers every other caller too.
     */
    static EbicsDownloadParams btdDownloadParams(ParsedArguments parsedArguments) {
        return new EbicsDownloadParams(
            upperCase(requireOption(parsedArguments.serviceName(), "--service")),
            upperCase(requireOption(parsedArguments.scope(), "--scope")),
            upperCase(parsedArguments.option()),
            requireOption(parsedArguments.messageName(), "--msg-name"),
            // Optional: bank lists like ZKB's result archive (OTH BIL CH004TPE msc) carry no version.
            normalize(parsedArguments.messageVersion()),
            upperCase(requireOption(parsedArguments.containerType(), "--container")),
            parseDate(parsedArguments.startDate(), "--start"),
            parseDate(parsedArguments.endDate(), "--end")
        );
    }

    /**
     * Service code, scope, service option and container type are EBICS code list values and are
     * always upper case. Message names like {@code camt.053} are not, and stay untouched.
     */
    private static String upperCase(String value) {
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    /**
     * Builds the date-range-only parameters of the legacy (EBICS 2.x) download path.
     */
    static EbicsDownloadParams legacyDownloadParams(ParsedArguments parsedArguments) {
        return EbicsDownloadParams.dateRangeOnly(
            parseDate(parsedArguments.startDate(), "--start"),
            parseDate(parsedArguments.endDate(), "--end")
        );
    }

    static String requireOutputPath(ParsedArguments parsedArguments) {
        return requireOption(parsedArguments.outputPath(), "-o");
    }

    private static String requireOption(String value, String option) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException("Missing required option " + option + " for --btd");
        }
        return normalized;
    }

    /**
     * Parses a {@code YYYY-MM-DD} argument into a calendar day. No timezone is involved, so the
     * day the user typed is the day that reaches the bank, wherever the job runs.
     */
    private static LocalDate parseDate(String value, String option) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Option " + option + " expects a date as YYYY-MM-DD but was: " + normalized);
        }
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
        if (orderType == OrderType.XTC) {
            // ZKB test platform: CSV input file for camt simulation (OTH BIL CH004TPS csv).
            // No message version in the bank's BTF list and no ES on a simulation input.
            var orderParams = new EbicsUploadParams.OrderParams(
                "OTH",
                "BIL",
                "CH004TPS",
                "csv",
                null,
                false
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
        private final Map<String, String> values;
        private final String inputPath;
        private final String outputPath;
        private final String startDate;
        private final String endDate;

        private ParsedArguments(
            Set<String> flags,
            Map<String, String> values,
            String inputPath,
            String outputPath,
            String startDate,
            String endDate
        ) {
            this.flags.addAll(flags);
            this.values = Map.copyOf(values);
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        /** Value options of the EBICS 3.0 service block; each consumes the following argument. */
        private static final Set<String> VALUE_OPTIONS = Set.of(
            "--service",
            "--scope",
            "--option",
            "--msg-name",
            "--msg-version",
            "--container"
        );

        static ParsedArguments parse(String[] args) {
            Set<String> flags = new LinkedHashSet<>();
            Map<String, String> values = new LinkedHashMap<>();
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
                    String lowered = arg.toLowerCase(Locale.ROOT);
                    if (VALUE_OPTIONS.contains(lowered)) {
                        values.put(lowered, requireValue(args, ++index, arg));
                        continue;
                    }
                    if (arg.startsWith("--")) {
                        flags.add(lowered);
                    }
                }
            }
            return new ParsedArguments(flags, values, inputPath, outputPath, startDate, endDate);
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

        String serviceName() {
            return values.get("--service");
        }

        String scope() {
            return values.get("--scope");
        }

        String option() {
            return values.get("--option");
        }

        String messageName() {
            return values.get("--msg-name");
        }

        String messageVersion() {
            return values.get("--msg-version");
        }

        String containerType() {
            return values.get("--container");
        }
    }
}
