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
    override val partner: Partner,
    override val ebicsVersion: EbicsVersion,
    override val userId: String,
    override val name: String,
    override val dn: String,
    override var userStatus: EbicsUserStatusEnum
) : EbicsUser, Serializable {
    companion object {
        @JvmStatic
        fun deserialize(ois: ObjectInputStream, partner: Partner): User = User(
            partner,
            ois.readObject() as EbicsVersion,
            ois.readUTF(),  //UserId
            ois.readUTF(),  //Name
            ois.readUTF(),  //DN
            (ois.readObject() as EbicsUserStatusEnum)
        )
    }

    override fun serialize(oos: ObjectOutputStream) {
        oos.writeObject(ebicsVersion)
        oos.writeUTF(userId)
        oos.writeUTF(name)
        oos.writeUTF(dn)
        oos.writeObject(userStatus)

        oos.flush()
        oos.close()
    }

    override val saveName: String = "user-${userId}.cer"
}