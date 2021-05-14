package org.ebics.client.user.serializable

import org.ebics.client.certificate.CertificateManager
import org.ebics.client.interfaces.PasswordCallback
import org.ebics.client.user.EbicsUserStatus
import org.ebics.client.user.EbicsVersion
import org.ebics.client.user.base.EbicsUser
import org.ebics.client.user.base.EbicsUserInfo
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.PrivateKey
import java.security.cert.X509Certificate

class User(
    override val userInfo: EbicsUserInfo,
    override val a005Certificate: X509Certificate,
    override val e002Certificate: X509Certificate,
    override val x002Certificate: X509Certificate,
    override val a005PrivateKey: PrivateKey,
    override val e002PrivateKey: PrivateKey,
    override val x002PrivateKey: PrivateKey,
    override val partner: Partner
) : EbicsUser, Serializable {
    companion object {

        @JvmStatic
        fun create(userInfo: EbicsUserInfo, partner: Partner): User {
            val manager = CertificateManager(userInfo)
            return User(
                userInfo,
                manager.a005Certificate,
                manager.e002Certificate,
                manager.x002Certificate,
                manager.a005PrivateKey,
                manager.e002PrivateKey,
                manager.x002PrivateKey,
                partner
            )
        }

        @JvmStatic
        fun saveCertificates(userInfo: EbicsUserInfo, keyStorePath: String, passwordCallback: PasswordCallback) {
            val manager = CertificateManager(userInfo)
            manager.save(keyStorePath, passwordCallback)
        }

        @JvmStatic
        fun loadCertificates(userInfo: EbicsUserInfo, partner: Partner, keyStorePath: String, passwordCallback: PasswordCallback): User {
            val manager = CertificateManager(userInfo)
            manager.load(keyStorePath, passwordCallback)
            return User(
                userInfo,
                manager.a005Certificate,
                manager.e002Certificate,
                manager.x002Certificate,
                manager.a005PrivateKey,
                manager.e002PrivateKey,
                manager.x002PrivateKey,
                partner
            )
        }

        @JvmStatic
        fun deserialize(ois: ObjectInputStream, partner: Partner): User = User(
            org.ebics.client.user.EbicsUserInfo(
                ois.readObject() as EbicsVersion,
                ois.readUTF(),  //UserId
                ois.readUTF(),  //Name
                ois.readUTF(),  //DN
                (ois.readObject() as EbicsUserStatus)),
            ois.readObject() as X509Certificate,
            ois.readObject() as X509Certificate,
            ois.readObject() as X509Certificate,
            ois.readObject() as PrivateKey,
            ois.readObject() as PrivateKey,
            ois.readObject() as PrivateKey,
            partner
        )
    }

    override fun serialize(oos: ObjectOutputStream) {
        with(userInfo) {
            oos.writeObject(ebicsVersion)
            oos.writeUTF(userId)
            oos.writeUTF(name)
            oos.writeUTF(dn)
            oos.writeObject(userStatus)
        }
        oos.writeObject(a005Certificate)
        oos.writeObject(e002Certificate)
        oos.writeObject(x002Certificate)
        oos.writeObject(a005PrivateKey)
        oos.writeObject(e002PrivateKey)
        oos.writeObject(x002PrivateKey)
        oos.flush()
        oos.close()
    }

    override val saveName: String = "user-${userInfo.userId}.cer"
}