package org.ebics.client.ebicsrestapi

import com.ninjasquad.springmockk.MockkBean
import org.ebics.client.api.trace.IFileService
import org.ebics.client.ebicsrestapi.bankconnection.UserServiceTestImpl
import org.ebics.client.ebicsrestapi.bankconnection.session.EbicsSessionCache
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionCache
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
import org.ebics.client.ebicsrestapi.utils.FileDownloadCache
import org.ebics.client.ebicsrestapi.utils.FileServiceMockImpl
import org.ebics.client.ebicsrestapi.utils.IFileDownloadCache
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@EnableCaching
@Lazy
class TestContext {
    @Bean(name = ["testCacheManager"])
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager("sessions")
    }

    @MockkBean
    private lateinit var configuration: EbicsRestConfiguration

    @Bean(name = ["testSessionCache"])
    fun sessionCache(): IEbicsSessionCache = EbicsSessionCache(
        UserServiceTestImpl(), configuration, EbicsProduct("testProd", "de", "JTO")
    )

    @Bean
    fun servletWebServerFactory(): ServletWebServerFactory {
        return TomcatServletWebServerFactory()
    }

    @Bean(name = ["TestFileDownloadCache"])
    fun fileDownloadCache(): IFileDownloadCache = FileDownloadCache(fileService())

    @Bean(name = ["FileServiceMockImpl"])
    fun fileService(): IFileService = FileServiceMockImpl()
}