package org.ebics.client.user

import org.ebics.client.api.EbicsVersion
import org.ebics.client.api.Partner
import org.ebics.client.certificate.CertificateManager
import org.ebics.client.interfaces.EbicsPartner
import org.ebics.client.interfaces.PasswordCallback
import org.ebics.client.interfaces.Savable
import org.ebics.client.user.base.EbicsUser
import org.ebics.client.user.base.EbicsUserInfoInt
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.PrivateKey
import java.security.cert.X509Certificate

class SerializableEbicsUser(
    override val userInfo: EbicsUserInfoInt,
    override val a005Certificate: X509Certificate,
    override val e002Certificate: X509Certificate,
    override val x002Certificate: X509Certificate,
    override val a005PrivateKey: PrivateKey,
    override val e002PrivateKey: PrivateKey,
    override val x002PrivateKey: PrivateKey,
    override val partner: EbicsPartner
) : EbicsUser, Savable {
    companion object {

        @JvmStatic
        fun create(userInfo: EbicsUserInfoInt, partner: Partner): SerializableEbicsUser {
            val manager = CertificateManager(userInfo)
            return SerializableEbicsUser(
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
        fun saveCertificates(userInfo: EbicsUserInfoInt, keyStorePath: String, passwordCallback: PasswordCallback) {
            val manager = CertificateManager(userInfo)
            manager.save(keyStorePath, passwordCallback)
        }

        @JvmStatic
        fun loadCertificates(userInfo: EbicsUserInfoInt, partner: Partner, keyStorePath: String, passwordCallback: PasswordCallback): SerializableEbicsUser {
            val manager = CertificateManager(userInfo)
            manager.load(keyStorePath, passwordCallback)
            return SerializableEbicsUser(
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
        fun deserialize(ois: ObjectInputStream, partner: Partner): SerializableEbicsUser = SerializableEbicsUser(
            EbicsUserInfo(
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

    fun serialize(oos: ObjectOutputStream) {
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

    override fun save(oos: ObjectOutputStream?) = serialize(oos!!)

    override fun getSaveName(): String {
        return "user-${userInfo.userId}.cer"
    }
}