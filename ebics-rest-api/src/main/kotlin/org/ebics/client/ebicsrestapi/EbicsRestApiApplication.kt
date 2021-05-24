package org.ebics.client.ebicsrestapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@ComponentScan("org.ebics.client.api", "org.ebics.client.ebicsrestapi")
@EntityScan("org.ebics.client.api")
@EnableJpaRepositories("org.ebics.client.api")
class EbicsRestApiApplication

fun main(args: Array<String>) {
	runApplication<EbicsRestApiApplication>(*args)
}

