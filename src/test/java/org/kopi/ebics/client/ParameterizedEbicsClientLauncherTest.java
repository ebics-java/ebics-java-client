package org.kopi.ebics.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void normalizeHandlesBlankValues() {
        assertNull(ParameterizedEbicsClientLauncher.normalize("   "));
        assertEquals("value", ParameterizedEbicsClientLauncher.normalize(" value "));
    }
}
