package org.ebics.client.api.bank

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.bank.versions.VersionSupport
import java.net.URL
import javax.persistence.*

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["bankURL","hostId"])])
data class Bank(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    override val bankURL: URL,
    override val hostId: String,
    override val name: String,

    @OneToOne(optional = true, cascade = [CascadeType.ALL])
    var keyStore: BankKeyStore?,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "bank")
    val ebicsVersions: List<VersionSupport>? = emptyList(),

    override val httpClientConfigurationName: String = "default"
) : EbicsBank
