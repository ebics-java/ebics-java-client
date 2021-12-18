package org.ebics.client.ebicsrestapi.bankconnection

import org.ebics.client.api.EbicsSession
import org.ebics.client.api.bank.Bank
import org.ebics.client.ebicsrestapi.bankconnection.session.CachedSession
import org.ebics.client.ebicsrestapi.bankconnection.session.EbicsSessionCache
import org.ebics.client.model.Product
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.IOException

@SpringBootTest
//@ContextConfiguration(classes = [EbicsSessionCache::class, EbicsRestConfiguration::class, UserServiceTestImpl::class, TraceService::class, TraceRepository::class])
class EbicsSessionCacheTest(@Autowired private val ebicsSessionCache: EbicsSessionCache) {

    val prod = Product("testProd", "de", "JTO")

    @Test
    fun ifSessionRequestedWithWrongPwd_Then_ThrowIoException() {
        Assertions.assertThrows(IOException::class.java) {
            ebicsSessionCache.getSession(
                UserIdPass(1, "WRONG_pass"), prod
            )
        }
    }

    @Test
    fun ifSessionWithBankKeysRequestedAndEmptyCache_Then_NewSessionCreated() {
        with(
            ebicsSessionCache.getSession(
                UserIdPass(1, "pass1"), prod
            )
        ) {
            Assertions.assertTrue(sessionId.isNotBlank())
            Assertions.assertTrue(sessionId.length == 36)
            Assertions.assertNotNull(session)
            Assertions.assertNotNull(session.userCert)
            Assertions.assertNotNull(session.user)
            Assertions.assertNotNull((session.user.partner.bank as Bank).keyStore)
        }
    }

    @Test
    fun ifSessionWithoutBankKeysRequestedAndEmptyCache_Then_NewSessionCreated() {
        with(
            ebicsSessionCache.getSession(
                UserIdPass(2, "pass2"), prod, false
            )
        ) {
            Assertions.assertTrue(sessionId.isNotBlank())
            Assertions.assertTrue(sessionId.length == 36)
            Assertions.assertNotNull(sessionId)
            Assertions.assertNotNull(session)
            Assertions.assertNotNull(session.userCert)
            Assertions.assertNotNull(session.user)
            Assertions.assertNull((session.user.partner.bank as Bank).keyStore)
        }
    }

    @Test
    fun ifTwoSessionsRequested_Then_TwoDifferentIdsMustBeReturned() {
        val s1 =
            ebicsSessionCache.getSession(
                UserIdPass(1, "pass1"), prod, true
            )
        val s2 =
            ebicsSessionCache.getSession(
                UserIdPass(2, "pass2"), prod, false
            )

        Assertions.assertNotEquals(s1.sessionId, s2.sessionId, "Session ID must be unique")
    }

    @Test
    fun ifTheNewSessionIsRequestedAndThenSameSessionOnceMore_Then_TheCachedSessionIsReturned() {
        val s1 =
            ebicsSessionCache.getSession(
                 UserIdPass(1, "pass1"), prod, true
            )
        val s2 =
            ebicsSessionCache.getSession(
                 UserIdPass(1, "pass1"), prod, true
            )

        Assertions.assertEquals(s1.sessionId, s2.sessionId, "Session ID must same")
        Assertions.assertEquals(s1.session, s2.session, "The returned session must be same")
    }

    @Test
    fun ifTheNewSessionIsRequestedAndThenSameSessionOnceMore_withWrongPwd_Then_IOException() {
        val s1 =
            ebicsSessionCache.getSession(
                UserIdPass(1, "pass1"), prod, true
            )
        Assertions.assertThrows(IOException::class.java) {
            val s2 =
                ebicsSessionCache.getSession(
                    UserIdPass(1, "pass1_WRONG"), prod, true
                )
        }
    }
}