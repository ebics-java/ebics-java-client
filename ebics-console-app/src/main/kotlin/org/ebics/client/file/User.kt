package org.ebics.client.file

import org.ebics.client.api.EbicsUser
import org.ebics.client.model.user.EbicsUserStatus
import org.ebics.client.model.EbicsVersion
import org.ebics.client.api.EbicsUserInfo
import org.ebics.client.api.Serializable
import org.ebics.client.model.user.EbicsUserStatusEnum
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
        fun deserialize(ois: ObjectInputStream, partner: Partner): User = User(
            UserInfo(
                ois.readObject() as EbicsVersion,
                ois.readUTF(),  //UserId
                ois.readUTF(),  //Name
                ois.readUTF(),  //DN
                (ois.readObject() as EbicsUserStatusEnum)
            ),
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