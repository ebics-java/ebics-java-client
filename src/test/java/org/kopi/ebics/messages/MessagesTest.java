package org.kopi.ebics.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class MessagesTest {

    @Test
    void getString() {
        // fallback to english
        Messages messages = new Messages("org.kopi.ebics.letter.messages", Locale.CHINA);
        assertEquals("dd.MM.yyyy", messages.getString("Letter.dateFormat"));
    }

    @Test
    void throwWhenUnknownBundle() {
        assertThrows(RuntimeException.class, new Executable() {
            @Override
            public void execute() {
                new Messages("org.kopi.ebics.letter.messages_unknown");
            }
        });
    }
}
