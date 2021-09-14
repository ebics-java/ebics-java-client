package org.ebics.client.api.bank.versions

import org.ebics.client.model.EbicsVersion
import java.io.Serializable

/**
 * Composite Key (IdClass) for VersionSupport Entity
 * The fields must have default value (doesn't matter what, JPA will set that correctly)
 * The foreign key type must be same as the key type in foreign entity (bank:Long, because Bank.id is Long)
 * The names of following fields must be exactly same as in entity VersionSupport
 * "bank": Long -> "bank": Bank
 */
data class VersionSupportId(
    val version: EbicsVersion = EbicsVersion.H005,
    val bank: Long = 1,
) : Serializable