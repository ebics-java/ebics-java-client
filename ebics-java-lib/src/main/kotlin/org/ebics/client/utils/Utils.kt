package org.ebics.client.utils

import org.apache.xmlbeans.XmlObject
import java.nio.charset.Charset
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

fun String.addSpaces(afterCharCount: Int): String = this.replace("(.{$afterCharCount})".toRegex(), "$1 ")

fun LocalDate.toDate(): Date {
    // Getting system timezone
    val systemTimeZone = ZoneId.systemDefault()

    // converting LocalDateTime to ZonedDateTime with the system timezone
    val zonedDateTime = this.atStartOfDay(systemTimeZone)

    // converting ZonedDateTime to Date using Date.from() and ZonedDateTime.toInstant()
    return Date.from(zonedDateTime.toInstant())
}

fun XmlObject.equalXml(xmlObject: XmlObject): Boolean = this.xmlText().equals(xmlObject.xmlText())


inline fun requireNotNullAndNotBlank(
    value: String?,
    lazyMessage: () -> Any = { "The string must not be blank or null" }
): String {
    val notNullValue = requireNotNull(value, lazyMessage)
    require(value.isNotBlank(), lazyMessage)
    return notNullValue
}

fun requireNotNullAndNotBlank(value: String?, stringName: String): String =
    requireNotNullAndNotBlank(value) { "The $stringName must not be null or blank" }

fun <T> requireNotNull(value: T?, paramName: String): T = requireNotNull(value) { "The $paramName must not be null" }

fun ByteArray.toStringSafe(
    charset: Charset = Charsets.UTF_8,
    nonUTF8default: String = "This is probably binary string for given encoding ${charset.displayName()}"
): String {
    val str = String(this, charset)
    val isUtf8 = str.toByteArray(charset).contentEquals(this)
    return if (isUtf8) str else nonUTF8default
}