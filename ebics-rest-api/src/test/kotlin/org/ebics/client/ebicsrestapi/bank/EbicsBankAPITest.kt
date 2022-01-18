package org.ebics.client.ebicsrestapi.bank

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkConstructor
import org.ebics.client.bank.BankOperations
import org.ebics.client.ebicsrestapi.EbicsAccessMode
import org.ebics.client.model.EbicsVersion
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.io.IOException
import java.net.URL

@SpringBootTest
@ExtendWith(MockKExtension::class)
@ContextConfiguration(classes=[EbicsBankAPITestContext::class])
class EbicsBankAPITest(@Autowired private val bankAPI: EbicsBankAPI) {
    @Test
    fun offlineTestEmpty() {
        val versions = bankAPI.getSupportedVersions(1, URL("http://test.url"), "bankhostid", "default", EbicsAccessMode.Offline)
        Assertions.assertTrue(versions.isEmpty())
    }

    @Test
    fun onlineTestTwoVersions() {
        mockkConstructor(BankOperations::class)
        every { anyConstructed<BankOperations>().sendHEV(any(), any(), any()) } returns listOf(EbicsVersion.H005, EbicsVersion.H004)
        val versions = bankAPI.getSupportedVersions(1, URL("http://test.url"), "bankhostid", "default", EbicsAccessMode.ForcedOnline)
        Assertions.assertTrue(versions.size == 2)
        with(versions.single{ it.version == EbicsVersion.H005 }) {
            Assertions.assertTrue(isSupportedByBank)
            Assertions.assertTrue(isSupportedByClient)
            Assertions.assertTrue(isAllowedForUse)
            Assertions.assertTrue(isPreferredForUse)
        }
        with(versions.single{ it.version == EbicsVersion.H004 }) {
            Assertions.assertTrue(isSupportedByBank)
            Assertions.assertTrue(isSupportedByClient)
            Assertions.assertTrue(isAllowedForUse)
            Assertions.assertFalse(isPreferredForUse)
        }
    }

    @Test
    fun onlineTestUnknownVersion() {
        mockkConstructor(BankOperations::class)
        every { anyConstructed<BankOperations>().sendHEV(any(), any(), any()) } returns listOf(EbicsVersion.H002)
        val versions = bankAPI.getSupportedVersions(1, URL("http://test.url"), "bankhostid", "default", EbicsAccessMode.ForcedOnline)
        Assertions.assertTrue(versions.size == 3)
        Assertions.assertFalse(versions.any{ it.isAllowedForUse })
    }

    @Test
    fun optionalOnlineTest() {
        mockkConstructor(BankOperations::class)
        every { anyConstructed<BankOperations>().sendHEV(any(), any(), any()) } throws IOException("error reading HEV")
        val versions = bankAPI.getSupportedVersions(2, URL("http://test.url"), "bankhostid", "default", EbicsAccessMode.OptionalOnline)
        Assertions.assertTrue(versions.size == 2)
        with(versions.single{ it.version == EbicsVersion.H005 }) {
            Assertions.assertTrue(isSupportedByBank)
            Assertions.assertTrue(isSupportedByClient)
            Assertions.assertTrue(isAllowedForUse)
            Assertions.assertTrue(isPreferredForUse)
        }
        with(versions.single{ it.version == EbicsVersion.H004 }) {
            Assertions.assertTrue(isSupportedByBank)
            Assertions.assertTrue(isSupportedByClient)
            Assertions.assertFalse(isAllowedForUse)
            Assertions.assertFalse(isPreferredForUse)
        }
    }
}