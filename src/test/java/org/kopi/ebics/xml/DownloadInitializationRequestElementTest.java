package org.kopi.ebics.xml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import org.kopi.ebics.client.EbicsDownloadParams;

class DownloadInitializationRequestElementTest {

    @Test
    void buildsBtdRequestWithSwissCamt053ServiceParams() throws Exception {
        var params = new EbicsDownloadParams(
            "EOP", "CH", null, "camt.053", "08", "ZIP",
            localDate(2026, 8, 10),
            localDate(2026, 8, 11));

        String raw = TestSessions.buildDownloadInitializationXml(params);
        System.out.println("=== BTD download initialization request ===");
        System.out.println(raw);
        System.out.println("=== end of request ===");

        String xml = stripNamespacePrefixes(raw);

        assertTrue(xml.contains("<AdminOrderType>BTD</AdminOrderType>"),
            "EBICS 3.0 verlangt BTD als AdminOrderType, nicht den 3-Buchstaben-Code");
        assertTrue(xml.contains("<ServiceName>EOP</ServiceName>"));
        assertTrue(xml.contains("<Scope>CH</Scope>"));
        assertTrue(xml.contains(">camt.053<"), "MsgName fehlt");
        assertTrue(xml.matches("(?s).*<MsgName[^>]*version=\"08\".*"), "MsgName-Version fehlt");
        assertTrue(xml.matches("(?s).*<Container[^>]*containerType=\"ZIP\".*"), "Container fehlt");
        assertTrue(xml.contains("<DateRange>"),
            "Ohne DateRange kann der naechtliche Job verpasste Tage nicht nachholen");
        assertTrue(xml.contains("<Start>2026-08-10</Start>"),
            "DateRange-Start muss ein reines xs:date ohne Zeitzonen-Offset sein");
        assertTrue(xml.contains("<End>2026-08-11</End>"),
            "DateRange-Ende muss ein reines xs:date ohne Zeitzonen-Offset sein");
    }

    /**
     * Der Kalendertag wird lokal gebildet, damit die Behauptung in jeder Zeitzone haelt.
     * Fixe Epoch-Millis wuerden westlich von UTC auf den Vortag rutschen.
     */
    private static Date localDate(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day)
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Der Auftragsparameter-Block muss gegen das H005-Schema gueltig sein, sonst lehnt die Bank ab. */
    @Test
    void btdOrderParamsAreSchemaValid() {
        var params = EbicsXmlFactory.createBTDParams("EOP", "CH", null, "camt.053", "08", "ZIP",
            localDate(2026, 8, 10), localDate(2026, 8, 11));

        var errors = new ArrayList<XmlError>();
        boolean valid = params.validate(new XmlOptions().setErrorListener(errors));

        assertTrue(valid, "BTDOrderParams ist nicht schemakonform: " + errors);
    }

    /** Ein unbekannter Container-Typ muss abbrechen statt still zu verschwinden. */
    @Test
    void rejectsUnknownContainerType() {
        assertThrows(IllegalArgumentException.class, () -> EbicsXmlFactory.createBTDParams(
            "EOP", "CH", null, "camt.053", "08", "TAR", null, null));
    }

    /** Ohne Service-Parameter muss der EBICS-2.x-Pfad unveraendert bleiben. */
    @Test
    void keepsLegacyRequestUnchangedWithoutParams() throws Exception {
        String xml = stripNamespacePrefixes(TestSessions.buildDownloadInitializationXml(null));

        assertTrue(xml.contains("<AdminOrderType>C53</AdminOrderType>"),
            "Ohne Parameter bleibt der 3-Buchstaben-Code der AdminOrderType");
        assertFalse(xml.contains("BTDOrderParams"), "Ohne Parameter darf kein BTD-Block entstehen");
        assertFalse(xml.contains("<DateRange>"), "Ohne Datumsbereich darf kein DateRange entstehen");
    }

    /**
     * EbicsClient.fetchFile(file, orderType, start, end) hat den Datumsbereich bisher verworfen.
     * Auf dem EBICS-2.x-Pfad landet er jetzt in StandardOrderParams, der Auftragstyp bleibt.
     */
    @Test
    void appliesDateRangeOnLegacyPathWithoutTurningIntoBtd() throws Exception {
        var params = EbicsDownloadParams.dateRangeOnly(
            localDate(2026, 8, 10), localDate(2026, 8, 11));

        String xml = stripNamespacePrefixes(TestSessions.buildDownloadInitializationXml(params));

        assertTrue(xml.contains("<AdminOrderType>C53</AdminOrderType>"),
            "Ohne Service-Namen darf kein BTD-Auftrag daraus werden");
        assertFalse(xml.contains("BTDOrderParams"), "Ohne Service-Namen kein BTD-Block");
        assertTrue(xml.contains("<Start>2026-08-10</Start>"),
            "Der Datumsbereich muss in der Anfrage landen, nicht verworfen werden");
        assertTrue(xml.contains("<End>2026-08-11</End>"));
    }

    /** XMLBeans waehlt Namensraum-Praefixe frei; die duerfen den Test nicht kippen. */
    private static String stripNamespacePrefixes(String xml) {
        return xml.replaceAll("<(/?)[A-Za-z0-9_.-]+:", "<$1")
                  .replaceAll("\\s+xmlns(:[A-Za-z0-9_.-]+)?=\"[^\"]*\"", "");
    }
}
