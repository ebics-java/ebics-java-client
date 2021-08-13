package org.ebics.client.utils

import java.time.LocalDate
import java.time.ZoneId
import java.util.*


/**
 * Convert HEX string to byte array
 */
fun String.decodeHexToByteArray(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

/**
 * Convert byte array to HEX string
 */
fun ByteArray.toHexString(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

fun LocalDate.toDate(): Date {
    // Getting system timezone
    val systemTimeZone = ZoneId.systemDefault()

    // converting LocalDateTime to ZonedDateTime with the system timezone
    val zonedDateTime = this.atStartOfDay(systemTimeZone)

    // converting ZonedDateTime to Date using Date.from() and ZonedDateTime.toInstant()
    return Date.from(zonedDateTime.toInstant())
}