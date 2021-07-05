package org.ebics.client.api.bank.cert

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ebics.client.api.bank.Bank
import org.ebics.client.certificate.BankCertificateManager
import java.security.interfaces.RSAPublicKey
import javax.persistence.*

@Entity
class BankKeyStore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    @Lob
    @Column(name = "e002Digest", columnDefinition="BLOB")
    val e002Digest: ByteArray,

    @Lob
    @Column(name = "x002Digest", columnDefinition="BLOB")
    val x002Digest: ByteArray,

    @Lob
    @Column(name = "e002Key", columnDefinition="BLOB")
    val e002Key: RSAPublicKey,

    @Lob
    @Column(name = "x002Key", columnDefinition="BLOB")
    val x002Key: RSAPublicKey,

    @JsonIgnore
    @OneToOne(mappedBy = "keyStore")
    val bank: Bank,
) {
    fun toBankCertMgr():BankCertificateManager = BankCertificateManager(e002Digest, x002Digest, e002Key, x002Key)
    companion object {
        fun fromBankCertMgr(bankCertificateManager: BankCertificateManager, bank: Bank): BankKeyStore =
            BankKeyStore(null, bankCertificateManager.e002Digest, bankCertificateManager.x002Digest, bankCertificateManager.e002Key, bankCertificateManager.x002Key, bank)
    }
}
