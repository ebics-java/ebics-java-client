package org.ebics.client.api.user

import org.ebics.client.api.EbicsUser
import org.ebics.client.api.cert.UserKeyStore
import org.ebics.client.api.partner.Partner
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatusEnum
import javax.persistence.*

@Entity
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    override val ebicsVersion: EbicsVersion,
    override val userId: String,
    override val name: String,
    override val dn: String,
    override var userStatus: EbicsUserStatusEnum = EbicsUserStatusEnum.CREATED,

    @ManyToOne(optional = false)
    @JoinColumn(name="PARTNER_ID")
    override val partner: Partner,

    @OneToOne(optional = true)
    val keyStore: UserKeyStore?
) : EbicsUser