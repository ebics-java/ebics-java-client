package org.ebics.client.ebicsrestapi.bankconnection.session

import com.ninjasquad.springmockk.MockkBean
import io.mockk.junit5.MockKExtension
import org.ebics.client.api.bank.Bank
import org.ebics.client.ebicsrestapi.EbicsProduct
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.UserServiceTestImpl
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@SpringBootTest
@ExtendWith(MockKExtension::class)
class EbicsSessionCacheTest(@Autowired private val ebicsSessionCache: IEbicsSessionCache) {

    @Configuration
    @EnableCaching
    internal class ContextConfiguration {
        @Bean
        fun cacheManager(): CacheManager {
            return ConcurrentMapCacheManager("sessions")
        }

        @MockkBean
        private lateinit var configuration: EbicsRestConfiguration

        @Bean
        fun sessionCache(): IEbicsSessionCache = EbicsSessionCache(
            UserServiceTestImpl(), configuration, EbicsProduct("testProd", "de", "JTO")
        )
    }

    @Test
    fun ifSessionRequestedWithWrongPwd_Then_ThrowIoException() {
        Assertions.assertThrows(Exception::class.java) {
            ebicsSessionCache.getSession(
                UserIdPass(1, "WRONG_pass")
            )
        }
    }

    @Test
    fun ifSessionWithBankKeysRequestedAndEmptyCache_Then_NewSessionCreated() {
        with(
            ebicsSessionCache.getSession(
                UserIdPass(1, "pass1")
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
                UserIdPass(2, "pass2"), false
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
                UserIdPass(1, "pass1"), true
            )
        val s2 =
            ebicsSessionCache.getSession(
                UserIdPass(2, "pass2"), false
            )

        Assertions.assertNotEquals(s1.sessionId, s2.sessionId, "Session ID must be unique")
    }

    @Test
    fun ifTheNewSessionIsRequestedAndThenSameSessionOnceMore_Then_TheCachedSessionIsReturned() {
        val s1 =
            ebicsSessionCache.getSession(
                UserIdPass(1, "pass1"), true
            )
        val s2 =
            ebicsSessionCache.getSession(
                UserIdPass(1, "pass1"), true
            )

        Assertions.assertEquals(s1.sessionId, s2.sessionId, "Session ID must same")
        Assertions.assertEquals(s1, s2, "The returned session must be same")
    }

    @Test
    fun ifTheNewSessionIsRequestedAndThenSameSessionOnceMore_withWrongPwd_Then_IOException() {
        val s1 =
            ebicsSessionCache.getSession(
                UserIdPass(1, "pass1"), true
            )
        Assertions.assertThrows(Exception::class.java) {
            val s2 =
                ebicsSessionCache.getSession(
                    UserIdPass(1, "pass1_WRONG"), true
                )
        }
    }
}