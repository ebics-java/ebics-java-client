package org.ebics.client.api.cert

import org.ebics.client.api.user.UserInfo
import javax.persistence.*

@Entity
data class UserKeyStore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    val keyStoreBytes: ByteArray,

    @OneToOne
    val userInfo: UserInfo
)
