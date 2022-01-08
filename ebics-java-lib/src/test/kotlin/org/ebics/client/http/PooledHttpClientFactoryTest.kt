package org.ebics.client.http

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PooledHttpClientFactoryTest {
    @Test
    fun createSimpleHttpClientWithoutProxyAndSllTSFromFactory() {
        val configuration = object : HttpClientConfiguration {
            override val sslTrustedStoreFile: String? = null
            override val sslTrustedStoreFilePassword: String? = null
            override val httpProxyHost: String? = null
            override val httpProxyPort: Int? = null
            override val httpProxyUser: String? = null
            override val httpProxyPassword: String? = null
            override val httpContentHeader: String = ""
            override val socketTimeoutMilliseconds: Int = 300000
            override val connectionTimeoutMilliseconds: Int = 300000
        }
        val poolConfig = object  : HttpClientGlobalConfiguration {
            override val connectionPoolMaxTotal = 5
            override val connectionPoolDefaultMaxPerRoute = 10
            override val namedClientConfigurations: Map<String, HttpClientConfiguration> = mapOf("default" to configuration)
        }
        val factory = PooledHttpClientFactory(poolConfig)
        val client = factory.getHttpClient("default")
        Assertions.assertNotNull(client)
    }

    @Test
    fun gettingDefaultClientWithoutProvidingSpecificConfiguration() {
        val poolConfig = object  : HttpClientGlobalConfiguration {
            override val connectionPoolMaxTotal = 5
            override val connectionPoolDefaultMaxPerRoute = 10
            override val namedClientConfigurations: Map<String, HttpClientConfiguration> = emptyMap()
        }
        val factory = PooledHttpClientFactory(poolConfig)
        val client = factory.getHttpClient("default")
        Assertions.assertNotNull(client)
    }

    @Test
    fun gettingClientFromFactoryForNonExistingConfiguration() {
        val configuration = object : HttpClientConfiguration {
            override val sslTrustedStoreFile: String? = null
            override val sslTrustedStoreFilePassword: String? = null
            override val httpProxyHost: String? = null
            override val httpProxyPort: Int? = null
            override val httpProxyUser: String? = null
            override val httpProxyPassword: String? = null
            override val httpContentHeader: String = ""
            override val socketTimeoutMilliseconds: Int = 300000
            override val connectionTimeoutMilliseconds: Int = 300000
        }
        val poolConfig = object  : HttpClientGlobalConfiguration {
            override val connectionPoolMaxTotal = 5
            override val connectionPoolDefaultMaxPerRoute = 10
            override val namedClientConfigurations: Map<String, HttpClientConfiguration> = mapOf("default" to configuration)
        }
        val factory = PooledHttpClientFactory(poolConfig)
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            factory.getHttpClient("test")
        }
    }
}