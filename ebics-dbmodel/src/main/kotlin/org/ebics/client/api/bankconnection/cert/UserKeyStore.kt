package org.ebics.client.api.bankconnection.cert

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.certificate.UserCertificateManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

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
    val user: BankConnectionEntity
) {
    fun toUserCertMgr(password: String):UserCertificateManager {
        val ins = ByteArrayInputStream(keyStoreBytes)
        return UserCertificateManager.load(ins, password, user.userId)
    }

    companion object {
        fun fromUserCertMgr(user:BankConnectionEntity, userCertMgr:UserCertificateManager, password: String):UserKeyStore {
            val bos = ByteArrayOutputStream(4096)
            userCertMgr.save(bos, password, user.userId)
            return UserKeyStore(null, bos.toByteArray(), user)
        }
    }
}
