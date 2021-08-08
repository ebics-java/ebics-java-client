package org.ebics.client.ebicsrestapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.ComponentScan
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
}

fun main(args: Array<String>) {
	runApplication<EbicsRestApiApplication>(*args)
}

