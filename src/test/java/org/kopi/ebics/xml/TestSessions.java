package org.kopi.ebics.xml;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import org.apache.xml.security.Init;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.kopi.ebics.client.EbicsDownloadParams;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;

/**
 * Test helper that builds EBICS request elements against a stubbed session, so the generated
 * XML can be asserted without a bank, keystore or persisted workspace.
 */
final class TestSessions {

    /** 32 bytes worth of hex characters; the production code hex-decodes the bank digests. */
    private static final byte[] DUMMY_DIGEST =
        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
            .getBytes(StandardCharsets.US_ASCII);

    static {
        Init.init();
        Security.addProvider(new BouncyCastleProvider());
    }

    private TestSessions() {
    }

    /**
     * Builds the download initialization request for the given service parameters and returns the
     * canonical XML.
     *
     * @param params the EBICS 3.0 service parameters, or {@code null} for the legacy path
     * @return the generated request XML
     */
    static String buildDownloadInitializationXml(EbicsDownloadParams params) throws Exception {
        var element = new DownloadInitializationRequestElement(stubSession(), OrderType.C53, params);
        element.buildInitialization();
        return element.toPrettyString();
    }

    private static EbicsSession stubSession() throws Exception {
        var session = mock(EbicsSession.class, RETURNS_DEEP_STUBS);
        when(session.getBankID()).thenReturn("EBICSHOST");
        when(session.getProduct().getLanguage()).thenReturn("de");
        when(session.getProduct().getName()).thenReturn("test-product");
        when(session.getConfiguration().getAuthenticationVersion()).thenReturn("X002");
        when(session.getConfiguration().getEncryptionVersion()).thenReturn("E002");
        when(session.getConfiguration().getRevision()).thenReturn(1);
        when(session.getConfiguration().getVersion()).thenReturn("H005");
        when(session.getUser().getUserId()).thenReturn("USER0001");
        when(session.getUser().getSecurityMedium()).thenReturn("0000");
        when(session.getUser().getPartner().getPartnerId()).thenReturn("PARTNER1");
        when(session.getUser().getPartner().getBank().getX002Digest()).thenReturn(DUMMY_DIGEST);
        when(session.getUser().getPartner().getBank().getE002Digest()).thenReturn(DUMMY_DIGEST);
        return session;
    }
}
