package org.ebics.client.ebicsrestapi.user

import org.ebics.client.interfaces.PasswordCallback

data class UserIdPass(val id:Long, val password:String) {
    val passCb: PasswordCallback = PasswordCallback { password.toCharArray() }
}
