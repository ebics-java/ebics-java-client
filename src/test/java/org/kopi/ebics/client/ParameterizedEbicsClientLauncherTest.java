package org.kopi.ebics.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
            exception.getMessage().contains("must be given together"),
            "A half date range must abort instead of being dropped silently: "
                + exception.getMessage()
        );
    }

    /**
     * I-1: the legacy (non-BTD) path dropped a half date range silently as well, and the former
     * System.err warning was gone. Aborting beats warning: a catch-up run that believes it asked
     * for a period but did not is the exact failure this order type exists to prevent.
     */
    @Test
    void rejectsHalfDateRangeOnLegacyPathToo() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{ "--c53", "-o", "auszug.xml", "-s", "2026-08-01" }
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ParameterizedEbicsClientLauncher.legacyDownloadParams(parsed)
        );
        assertTrue(
            exception.getMessage().contains("must be given together"),
            "The legacy path must not silently drop a half date range: " + exception.getMessage()
        );
    }

    /** M-4: a reversed range is schema-valid and indistinguishable from "no data available". */
    @Test
    void rejectsReversedDateRange() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{
                "--btd", "--service", "EOP", "--scope", "CH", "--msg-name", "camt.053",
                "--msg-version", "08", "--container", "ZIP",
                "-s", "2026-08-11", "-e", "2026-08-10"
            }
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ParameterizedEbicsClientLauncher.btdDownloadParams(parsed)
        );
        assertTrue(
            exception.getMessage().contains("must not be before"),
            "Expected the reversed range to be named: " + exception.getMessage()
        );
    }

    /** M-1: the container type is an EBICS code list value, casing is not the user's problem. */
    @Test
    void normalizesCaseOfServiceCodes() {
        var parsed = ParameterizedEbicsClientLauncher.ParsedArguments.parse(
            new String[]{
                "--btd", "--service", "eop", "--scope", "ch", "--msg-name", "camt.053",
                "--msg-version", "08", "--container", "zip"
            }
        );

        var params = ParameterizedEbicsClientLauncher.btdDownloadParams(parsed);

        assertEquals("ZIP", params.containerType(), "--container zip must not abort");
        assertEquals("EOP", params.serviceName(), "service codes are upper case in EBICS");
        assertEquals("CH", params.scope(), "the scope is an ISO country or issuer code");
        assertEquals("camt.053", params.messageName(), "message names stay as given");
    }

    /**
     * I-3: every argument has to be checked before anything reaches the bank. The guard used to sit
     * after loadUser/createUser and after --ini/--hia/--hpb, so an incomplete --btd order could
     * still fire an INI request first, and INI is one-shot at most banks.
     *
     * <p>Proven by ordering: with no EBICS_* environment set, main() must fail on the argument, not
     * on the environment variable it reads later.
     */
    @Test
    void validatesArgumentsBeforeAnyBankContact() {
        assumeTrue(System.getenv("EBICS_PASSWORD") == null,
            "needs an environment without live EBICS credentials");

        Exception exception = assertThrows(
            IllegalArgumentException.class,
            () -> ParameterizedEbicsClientLauncher.main(new String[]{
                "--ini", "--btd", "--service", "EOP", "--scope", "CH",
                "--msg-name", "camt.053", "--msg-version", "08", "-o", "statement.zip"
            })
        );
        assertTrue(
            exception.getMessage().contains("Missing required option --container"),
            "Arguments must be rejected before the first environment read or bank call, got: "
                + exception.getMessage()
        );
    }

    @Test
    void normalizeHandlesBlankValues() {
        assertNull(ParameterizedEbicsClientLauncher.normalize("   "));
        assertEquals("value", ParameterizedEbicsClientLauncher.normalize(" value "));
    }
}
