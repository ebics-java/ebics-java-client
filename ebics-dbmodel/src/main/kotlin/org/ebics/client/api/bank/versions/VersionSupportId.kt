package org.ebics.client.api.bank.versions

import org.ebics.client.model.EbicsVersion
import java.io.Serializable

data class VersionSupportId(
    val version: EbicsVersion = EbicsVersion.H005,
    val bank: Long = 1,
) : Serializable