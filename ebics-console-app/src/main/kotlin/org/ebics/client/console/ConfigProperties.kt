package org.ebics.client.console

import java.io.File
import java.io.FileInputStream
import java.util.*

class ConfigProperties(file: File) {
    private val properties = Properties()
    operator fun get(key: String): String {
        val value = properties.getProperty(key)
        require(!(value == null || value.isEmpty())) { "property not set or empty: $key" }
        return value.trim { it <= ' ' }
    }

    init {
        properties.load(FileInputStream(file))
    }
}