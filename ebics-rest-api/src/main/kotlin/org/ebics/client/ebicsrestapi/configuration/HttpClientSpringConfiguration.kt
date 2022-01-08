package org.ebics.client.ebicsrestapi.configuration

import org.ebics.client.http.HttpClientConfiguration
import org.ebics.client.http.HttpClientGlobalConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties
class HttpClientSpringConfiguration (
    @Value("\${http.connectionPoolMaxTotal:20}")
    override val connectionPoolMaxTotal: Int,

    @Value("\${http.connectionPoolDefaultMaxPerRoute:5}")
    override val connectionPoolDefaultMaxPerRoute: Int,

    @Value("#{\${http.client.configurations:{:}}}")
    override val namedClientConfigurations: Map<String, HttpClientConfiguration>,
) : HttpClientGlobalConfiguration