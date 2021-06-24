package org.ebics.client.api.bank

import org.ebics.client.api.EbicsBank
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
    override val name: String
) : EbicsBank
