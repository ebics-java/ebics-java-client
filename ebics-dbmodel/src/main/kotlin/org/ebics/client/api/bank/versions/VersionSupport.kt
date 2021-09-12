package org.ebics.client.api.bank.versions

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ebics.client.api.bank.Bank
import org.ebics.client.model.EbicsVersion
import javax.persistence.*

@Entity(name = "EbicsVersionSupport")
@IdClass(VersionSupportId::class)
class VersionSupport(
    @Id
    @Column(name = "version")
    val version: EbicsVersion,

    val isSupportedByBank: Boolean,
    val isSupportedByClient: Boolean,
    val isAllowed: Boolean,
    val isDefault: Boolean,

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "bankId", referencedColumnName = "id")
    @Id
    val bank: Bank,
) {
    companion object {
        fun fromBaseAndBank(base: VersionSupportBase, bank: Bank): VersionSupport = VersionSupport(
            base.version, base.isSupportedByBank, base.isSupportedByClient, base.isAllowedForUse, base.isPreferredForUse, bank
        )
    }
}