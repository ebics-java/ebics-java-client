package org.ebics.client.ebicsrestapi

import com.ninjasquad.springmockk.MockkBean
import org.ebics.client.api.trace.IFileService
import org.ebics.client.ebicsrestapi.bankconnection.UserServiceTestImpl
import org.ebics.client.ebicsrestapi.bankconnection.session.EbicsSessionFactory
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionFactory
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
import org.ebics.client.ebicsrestapi.utils.FileDownloadCache
import org.ebics.client.ebicsrestapi.utils.FileServiceMockImpl
import org.ebics.client.ebicsrestapi.utils.IFileDownloadCache
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@Lazy
class TestContext {

    @MockkBean
    private lateinit var configuration: EbicsRestConfiguration

    @Bean(name = ["testSessionFactory"])
    fun sessionFactory(): IEbicsSessionFactory = EbicsSessionFactory(
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