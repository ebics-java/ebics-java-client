package org.ebics.client.ebicsrestapi.bankconnection.session

import org.ebics.client.api.bank.Bank
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.model.Product
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.IOException

@SpringBootTest
//@ContextConfiguration
class EbicsSessionCacheTest(@Autowired private val ebicsSessionCache: IEbicsSessionCache) {

    /*@Configuration
    internal class ContextConfiguration {

        @Bean
        fun traceService(): TraceService = TraceService(traceRepository, true)

        @Bean
        fun sessionCache(): IEbicsSessionCache = EbicsSessionCache(UserServiceTestImpl(),
            EbicsRestConfiguration(null,null,null,null,null, "",
            "", "", true, true, "en",
            traceService()))
    }*/

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
            Assertions.assertNotNull(this)
            Assertions.assertTrue(sessionId.isNotBlank())
            Assertions.assertTrue(sessionId.length == 36)
            Assertions.assertNotNull(userCert)
            Assertions.assertNotNull(user)
            Assertions.assertNotNull((user.partner.bank as Bank).keyStore)
        }
    }

    @Test
    fun ifSessionWithoutBankKeysRequestedAndEmptyCache_Then_NewSessionCreated() {
        with(
            ebicsSessionCache.getSession(
                UserIdPass(2, "pass2"), prod, false
            )
        ) {
            Assertions.assertNotNull(this)
            Assertions.assertTrue(sessionId.isNotBlank())
            Assertions.assertTrue(sessionId.length == 36)
            Assertions.assertNotNull(sessionId)
            Assertions.assertNotNull(userCert)
            Assertions.assertNotNull(user)
            Assertions.assertNull((user.partner.bank as Bank).keyStore)
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
        Assertions.assertEquals(s1, s2, "The returned session must be same")
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