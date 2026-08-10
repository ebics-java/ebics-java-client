package org.kopi.ebics.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ParameterizedEbicsClientLauncherTest {
    @Test
    void parsesFlagsAndInputOutputOptions() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{ "--create", "--ini", "--sta", "-o", "sta.xml", "-s", "2026-01-01" }
        );

        assertTrue(parsed.hasFlag("--create"));
        assertTrue(parsed.hasFlag("--ini"));
        assertEquals("--sta", parsed.firstOrderFlag());
        assertEquals("sta.xml", parsed.outputPath());
        assertEquals("2026-01-01", parsed.startDate());
    }

    @Test
    void ignoresReservedFlagsWhenResolvingOrder() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{ "--create", "--ini", "--hpb" }
        );

        assertNull(parsed.firstOrderFlag());
    }

    @Test
    void rejectsMissingOptionValue() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ParameterizedEbicsClientLauncher.ParsedArguments.parse(new String[]{ "-o" })
        );
        assertTrue(exception.getMessage().contains("Missing value for option -o"));
    }

    @Test
    void parsesBtdServiceOptions() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{
                "--btd", "--service", "EOP", "--scope", "CH", "--msg-name", "camt.053",
                "--msg-version", "08", "--container", "ZIP",
                "-s", "2026-08-10", "-e", "2026-08-11", "-o", "statement.zip"
            }
        );

        assertTrue(parsed.hasFlag("--btd"));
        assertNull(parsed.firstOrderFlag(), "--btd is reserved and must not be read as order type");

        var params = ParameterizedEbicsClientLauncher.btdDownloadParams(parsed);

        assertEquals("EOP", params.serviceName());
        assertEquals("CH", params.scope());
        assertEquals("camt.053", params.messageName());
        assertEquals("08", params.messageVersion());
        assertEquals("ZIP", params.containerType());
        assertNull(params.option());
        assertNotNull(params.startDate());
        assertNotNull(params.endDate());
        assertEquals("statement.zip", ParameterizedEbicsClientLauncher.requireOutputPath(parsed));
    }

    @Test
    void rejectsBtdWithoutMandatoryServiceValues() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{ "--btd", "--service", "EOP", "-o", "statement.zip" }
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ParameterizedEbicsClientLauncher.btdDownloadParams(parsed)
        );
        assertTrue(
            exception.getMessage().contains("Missing required option --scope for --btd"),
            "Expected a clear abort naming the missing option, got: " + exception.getMessage()
        );
    }

    @Test
    void rejectsBtdWithoutOutputPath() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{
                "--btd", "--service", "EOP", "--scope", "CH", "--msg-name", "camt.053",
                "--msg-version", "08", "--container", "ZIP"
            }
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ParameterizedEbicsClientLauncher.requireOutputPath(parsed)
        );
        assertTrue(exception.getMessage().contains("Missing required option -o for --btd"));
    }

    @Test
    void rejectsMalformedDateRange() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{
                "--btd", "--service", "EOP", "--scope", "CH", "--msg-name", "camt.053",
                "--msg-version", "08", "--container", "ZIP",
                "-s", "10.08.2026", "-e", "2026-08-11"
            }
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ParameterizedEbicsClientLauncher.btdDownloadParams(parsed)
        );
        assertTrue(exception.getMessage().contains("--start expects a date as YYYY-MM-DD"));
    }

    @Test
    void rejectsHalfDateRange() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{
                "--btd", "--service", "EOP", "--scope", "CH", "--msg-name", "camt.053",
                "--msg-version", "08", "--container", "ZIP", "-s", "2026-08-10"
            }
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ParameterizedEbicsClientLauncher.btdDownloadParams(parsed)
        );
        assertTrue(
            exception.getMessage().contains("--start and --end must be given together"),
            "A half date range must abort instead of being dropped silently: "
                + exception.getMessage()
        );
    }

    @Test
    void normalizeHandlesBlankValues() {
        assertNull(ParameterizedEbicsClientLauncher.normalize("   "));
        assertEquals("value", ParameterizedEbicsClientLauncher.normalize(" value "));
    }
}
