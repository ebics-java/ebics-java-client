package org.ebics.client.api.user

import org.ebics.client.api.EbicsUserInfo
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatusEnum

data class UserInfo(
    override val ebicsVersion: EbicsVersion,
    override val userId: String,
    override val name: String,
    override val dn: String,
    override var userStatus: EbicsUserStatusEnum
) : EbicsUserInfo
