package org.ebics.client.ebicsrestapi.bankconnection

interface IUserIdPass : IUserPass {
    val id:Long
    override val password:String
}