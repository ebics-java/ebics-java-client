package org.ebics.client.api.bank.versions

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ebics.client.api.bank.Bank
import org.ebics.client.model.EbicsVersion
import javax.persistence.*

@Entity(name = "EbicsVersionSupport")
class VersionSupport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    val version: EbicsVersion,
    val isSupportedByBank: Boolean,
    val isSupportedByClient: Boolean,
    val isAllowedForUse: Boolean,
    val isPreferredForUse: Boolean,

    @JsonIgnore
    @ManyToOne(optional = false)
    val bank: Bank,
) {
    companion object {
        fun fromBaseAndBank(base: VersionSupportBase, bank: Bank): VersionSupport = VersionSupport(
            base.id, base.version, base.isSupportedByBank, base.isSupportedByClient, base.isAllowedForUse, base.isPreferredForUse, bank
        )
    }
}