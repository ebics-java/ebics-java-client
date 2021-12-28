package org.ebics.client.ebicsrestapi.bankconnection

data class UserIdPass(override val id:Long, override val password:String) : IUserIdPass
