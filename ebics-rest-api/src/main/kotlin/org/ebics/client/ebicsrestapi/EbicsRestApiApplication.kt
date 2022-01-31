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
import java.io.File

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
            try {
                val resources = mutableListOf<Resource>(ClassPathResource("application.yml"))
                val configHome = System.getProperty("EWC_CONFIG_HOME") ?: System.getenv("EWC_CONFIG_HOME")
                if (configHome != null) {
                    val configFileNameProp = "$configHome/config.properties"
                    val configFileNameYaml = "$configHome/config.yaml"
                    when {
                        File(configFileNameProp).exists() -> resources.add(FileSystemResource(configFileNameProp))
                        File(configFileNameYaml).exists() -> resources.add(FileSystemResource(configFileNameYaml))
                        else -> throw IllegalArgumentException("Config file not found, please check your config folder $configHome, non of $configFileNameProp, $configFileNameYaml exist")
                    }
                    val logbackFile = "$configHome/logback.xml"
                    if (!File(logbackFile).exists())
                        throw IllegalArgumentException("$logbackFile doesn't exist, please provide valid logback file")
                } else {
                    println("EWC_CONFIG_HOME is not set, all mandatory external properties must be set as system or environment variable")
                }
                setLocations(*resources.toTypedArray())
            } catch (ex:Exception) {
                System.err.println("Failed to start application -> failed to configure property placeholder: $ex")
                throw ex
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<EbicsRestApiApplication>(*args)
}

