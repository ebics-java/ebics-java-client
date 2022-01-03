package org.ebics.client.api

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.sql.Driver
import java.sql.DriverManager

@ExtendWith(SpringExtension::class)
class JdbcDriverTest {
    @Test
    fun testH2JdbcDriver() {
        val configuredDriver: Driver = DriverManager.getDriver("jdbc:h2:url")
        Assertions.assertNotNull(configuredDriver)
    }

    @Test
    fun testPgSQLJdbcDriver() {
        val configuredDriver: Driver = DriverManager.getDriver("jdbc:postgresql:url")
        Assertions.assertNotNull(configuredDriver)
    }
}