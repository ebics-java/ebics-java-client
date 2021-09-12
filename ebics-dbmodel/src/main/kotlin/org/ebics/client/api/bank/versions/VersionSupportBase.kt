package org.ebics.client.api.bank.versions

import org.ebics.client.model.EbicsVersion

class VersionSupportBase(
    val id: Long?,
    val version: EbicsVersion,
    val isSupportedByBank: Boolean,
    val isSupportedByClient: Boolean,
    val isAllowedForUse: Boolean,
    val isPreferredForUse: Boolean,
)
