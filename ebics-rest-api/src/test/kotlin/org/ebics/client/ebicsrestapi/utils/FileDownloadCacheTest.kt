package org.ebics.client.ebicsrestapi.utils

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkConstructor
import org.apache.xml.security.Init
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.api.trace.IFileService
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.ebicsrestapi.EbicsProduct
import org.ebics.client.ebicsrestapi.MockSession
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.EbicsAdminOrderType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayOutputStream
import java.security.Security
import java.time.ZonedDateTime

@SpringBootTest
@ExtendWith(MockKExtension::class)
class FileDownloadCacheTest(
    @Autowired private val fileDownloadCache: IFileDownloadCache,
    @Autowired private val fileService: IFileService
) {
    init {
        Init.init()
        Security.addProvider(BouncyCastleProvider())
    }

    @MockkBean
    private lateinit var configuration: EbicsRestConfiguration

    @Configuration
    internal class TestContextConfiguration {
        @Bean
        fun fileDownloadCache(): IFileDownloadCache = FileDownloadCache(fileService())

        @Bean
        fun fileService(): IFileService = FileServiceMockImpl()
    }

    val prod = EbicsProduct("testProd", "de", "JTO")

    @BeforeEach
    fun initTestContext() {
        //Mock getting files from EBICS
        val bos = ByteArrayOutputStream()
        bos.write("firstLiveDownload".toByteArray())
        mockkConstructor(org.ebics.client.filetransfer.h005.FileTransferSession::class)
        every { anyConstructed<org.ebics.client.filetransfer.h005.FileTransferSession>().fetchFile(any()) } returns bos

        //Remove all stored entries from cache (the fileDownloadCache has state)
        fileService.removeAllFilesOlderThan(ZonedDateTime.now())
        //fileDownloadCache.houseKeepFiles(ZonedDateTime.now())
    }

    @Test
    fun whenCalledOnceWithoutCache_thenMustRetrieveOnline() {
        val session = MockSession.getSession(1, false, prod, configuration)
        val result =
            fileDownloadCache.getLastFileCached(
                session,
                OrderTypeDefinition(EbicsAdminOrderType.HTD),
                EbicsVersion.H005,
                false
            )
        Assertions.assertEquals("firstLiveDownload", String(result))
    }

    @Test
    fun whenCalledTwiceWithoutCache_thenMustRetrieveBothOnline() {
        val session = MockSession.getSession(1, false, prod, configuration)
        val result =
            fileDownloadCache.getLastFileCached(
                session,
                OrderTypeDefinition(EbicsAdminOrderType.HTD),
                EbicsVersion.H005,
                false
            )
        Assertions.assertEquals("firstLiveDownload", String(result))
        val result2 =
            fileDownloadCache.getLastFileCached(
                session,
                OrderTypeDefinition(EbicsAdminOrderType.HTD),
                EbicsVersion.H005,
                false
            )
        Assertions.assertEquals("firstLiveDownload", String(result2))
    }

    @Test
    fun whenCalledOnce_thenMustRetrieveOnline() {
        val session = MockSession.getSession(1, false, prod, configuration)
        val result =
            fileDownloadCache.getLastFileCached(
                session,
                OrderTypeDefinition(EbicsAdminOrderType.HTD),
                EbicsVersion.H005
            )
        Assertions.assertEquals("firstLiveDownload", String(result))
    }

    @Test
    fun whenCalledTwice_thenSecondMustRetrieveFromCache() {
        val session = MockSession.getSession(1, false, prod, configuration)
        val result =
            fileDownloadCache.getLastFileCached(
                session,
                OrderTypeDefinition(EbicsAdminOrderType.HTD),
                EbicsVersion.H005
            )
        Assertions.assertEquals("firstLiveDownload", String(result))

        val result2 =
            fileDownloadCache.getLastFileCached(
                session,
                OrderTypeDefinition(EbicsAdminOrderType.HTD),
                EbicsVersion.H005
            )
        Assertions.assertEquals("firstLiveDownload-cached", String(result2))
    }
}