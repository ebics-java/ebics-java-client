package org.kopi.ebics.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The constructor takes a {@link Properties} object, so callers must be able to override
 * the values bundled in config.properties. Before this test existed, getString() read the
 * ResourceBundle only and silently ignored everything the caller passed in — which pinned
 * every request to Version="H003" (EBICS 2.4) even inside an H005 (EBICS 3.0) namespace.
 */
class DefaultConfigurationTest {

    @Test
    void callerPropertiesOverrideTheBundledProtocolVersion(@TempDir File rootDir) {
        Properties properties = new Properties();
        properties.setProperty("ebics.version", "H005");

        DefaultConfiguration configuration = new DefaultConfiguration(rootDir, properties);

        assertEquals("H005", configuration.getVersion(),
            "the caller asked for EBICS 3.0; the bundled default H003 must not win");
    }

    @Test
    void callerPropertiesOverrideTheBundledCryptoVersions(@TempDir File rootDir) {
        Properties properties = new Properties();
        properties.setProperty("signature.version", "A006");
        properties.setProperty("authentication.version", "X003");
        properties.setProperty("encryption.version", "E003");

        DefaultConfiguration configuration = new DefaultConfiguration(rootDir, properties);

        assertEquals("A006", configuration.getSignatureVersion());
        assertEquals("X003", configuration.getAuthenticationVersion());
        assertEquals("E003", configuration.getEncryptionVersion());
    }

    @Test
    void bundledDefaultsStillApplyWhenTheCallerSaysNothing(@TempDir File rootDir) {
        DefaultConfiguration configuration = new DefaultConfiguration(rootDir, new Properties());

        assertEquals("A005", configuration.getSignatureVersion());
        assertEquals("X002", configuration.getAuthenticationVersion());
        assertEquals("E002", configuration.getEncryptionVersion());
        assertEquals("H003", configuration.getVersion(),
            "unchanged upstream default — this test guards against silently changing it");
    }

    @Test
    void directoryNamesStillComeFromTheBundleWhenNotOverridden(@TempDir File rootDir) {
        DefaultConfiguration configuration = new DefaultConfiguration(rootDir, new Properties());

        assertEquals("serialized", configuration.getSerializationDirectory().getName());
        assertEquals("users", configuration.getUsersDirectory().getName());
    }
}
