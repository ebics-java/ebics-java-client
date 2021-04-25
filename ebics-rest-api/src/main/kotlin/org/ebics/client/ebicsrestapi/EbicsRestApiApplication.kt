package org.ebics.client.ebicsrestapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EbicsRestApiApplication

fun main(args: Array<String>) {
	runApplication<EbicsRestApiApplication>(*args)
}

