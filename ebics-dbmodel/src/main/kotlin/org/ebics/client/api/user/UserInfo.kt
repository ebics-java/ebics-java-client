package org.ebics.client.api.user

import org.ebics.client.api.EbicsUserInfo
import org.ebics.client.api.cert.UserKeyStore
import org.ebics.client.model.user.EbicsUserStatus
import org.ebics.client.model.EbicsVersion
import javax.persistence.*

@Entity
data class UserInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long,

    override val ebicsVersion: EbicsVersion,
    override val userId: String,
    override val name: String,
    override val dn: String,
    override val userStatus: EbicsUserStatus,

    @OneToOne(optional = true)
    val keyStore: UserKeyStore?
): EbicsUserInfo
