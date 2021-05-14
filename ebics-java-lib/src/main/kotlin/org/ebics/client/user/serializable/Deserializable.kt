package org.ebics.client.user.serializable

import java.io.ObjectInputStream

interface Deserializable {
    fun deserialize(ois:ObjectInputStream) : Any
}