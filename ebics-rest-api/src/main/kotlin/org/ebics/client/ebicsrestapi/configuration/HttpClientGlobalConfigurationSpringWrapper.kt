package org.ebics.client.ebicsrestapi.configuration

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.ebics.client.http.HttpClientGlobalConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "http.client")
//Restrict JSON exposed fields to selected class (so the spring proxy parts like $$beanFactory are omitted)
@JsonSerialize(`as` = HttpClientGlobalConfiguration::class)
class HttpClientGlobalConfigurationSpringWrapper: HttpClientGlobalConfiguration {
    override var connectionPoolMaxTotal: Int = 25
    override var connectionPoolDefaultMaxPerRoute: Int = 5
    override var configurations: Map<String, HttpClientConfigurationSpringWrapper> = mapOf("default" to HttpClientConfigurationSpringWrapper())
}