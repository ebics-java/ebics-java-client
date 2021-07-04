package org.ebics.client.api.bank

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ebics.client.api.EbicsBank
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.user.cert.UserKeyStore
import java.net.URL
import java.security.interfaces.RSAPublicKey
import javax.persistence.*

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["bankURL","hostId"])])
data class Bank(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    override val bankURL: URL,
    override val useCertificate: Boolean,
    override val hostId: String,
    override val name: String,

    @JsonIgnore
    @OneToOne(optional = true, cascade = [CascadeType.ALL])
    var keyStore: BankKeyStore?
) : EbicsBank
