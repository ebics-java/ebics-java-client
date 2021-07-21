package org.ebics.client.api.user.cert

import com.fasterxml.jackson.annotation.JsonIgnore
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.ebics.client.api.user.User
import org.ebics.client.certificate.UserCertificateManager

import javax.persistence.*

/**
 * This entity represents the encrypted private and public keys of user,
 * The encryption is done using password
 */
@Entity
data class UserKeyStore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    @Lob
    @Column(name = "keyStoreBytes", columnDefinition="BLOB")
    val keyStoreBytes: ByteArray,

    @JsonIgnore
    @OneToOne(mappedBy = "keyStore")
    val user: User
) {
    fun toUserCertMgr(password: String):UserCertificateManager {
        val ins = ByteInputStream(keyStoreBytes, keyStoreBytes.size)
        return UserCertificateManager.load(ins, password, user.userId)
    }

    companion object {
        fun fromUserCertMgr(user:User, userCertMgr:UserCertificateManager, password: String):UserKeyStore {
            val bos = ByteOutputStream(4096)
            userCertMgr.save(bos, password, user.userId)
            return UserKeyStore(null, bos.bytes, user)
        }
    }
}
