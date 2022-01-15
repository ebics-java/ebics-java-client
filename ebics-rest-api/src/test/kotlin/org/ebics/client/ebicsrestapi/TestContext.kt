package org.ebics.client.ebicsrestapi

import com.ninjasquad.springmockk.MockkBean
import org.ebics.client.ebicsrestapi.bankconnection.UserServiceTestImpl
import org.ebics.client.ebicsrestapi.bankconnection.session.EbicsSessionCache
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionCache
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
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
}