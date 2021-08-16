package org.ebics.client.ebicsrestapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@ComponentScan("org.ebics.client.api", "org.ebics.client.ebicsrestapi")
@EntityScan("org.ebics.client.api")
@EnableJpaRepositories("org.ebics.client.api")
class EbicsRestApiApplication : SpringBootServletInitializer() {

	/**
	 * Start app as war file
	 */
	override fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder {
		return builder.sources(EbicsRestApiApplication::class.java)
	}

	/**
	 * This allow external config file on path defined by system or env variable "EWC_CONFIG_HOME"
	 * set EWC_CONFIG_HOME=../path/to/configuration/
	 */
	@Bean
	fun propertyResolver(): PropertySourcesPlaceholderConfigurer {
		return PropertySourcesPlaceholderConfigurer().apply {
			val resources = mutableListOf<Resource>(ClassPathResource("application.yml"))
			val configHome = System.getProperty("EWC_CONFIG_HOME") ?: System.getenv("EWC_CONFIG_HOME")
			if (configHome != null) {
				resources.add(FileSystemResource("$configHome/config.properties").apply { setIgnoreResourceNotFound(true) })
				resources.add(FileSystemResource("$configHome/config.yaml").apply { setIgnoreResourceNotFound(true) })
			}
			setLocations(*resources.toTypedArray())
		}
	}
}

fun main(args: Array<String>) {
	runApplication<EbicsRestApiApplication>(*args)
}

