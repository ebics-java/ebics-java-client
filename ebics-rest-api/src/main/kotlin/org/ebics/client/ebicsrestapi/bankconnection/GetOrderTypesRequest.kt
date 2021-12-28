package org.ebics.client.ebicsrestapi.bankconnection

data class GetOrderTypesRequest(
    override val password: String,
    val useCache: Boolean
) : IUserPass
