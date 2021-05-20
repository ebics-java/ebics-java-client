package org.ebics.client.api.user

import org.ebics.client.api.EbicsPartner
import org.ebics.client.api.EbicsUser
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.persistence.*

@Entity
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    override val userInfo: UserInfo,
    override val a005Certificate: X509Certificate,
    override val e002Certificate: X509Certificate,
    override val x002Certificate: X509Certificate,
    override val a005PrivateKey: PrivateKey,
    override val e002PrivateKey: PrivateKey,
    override val x002PrivateKey: PrivateKey,

    @ManyToOne(optional = false)
    @JoinColumn(name="PARTNER_ID")
    override val partner: EbicsPartner

) : EbicsUser