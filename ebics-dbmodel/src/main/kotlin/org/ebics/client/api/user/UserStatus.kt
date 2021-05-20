package org.ebics.client.api.user

import org.ebics.client.model.user.EbicsUserStatus
import org.ebics.client.model.user.EbicsUserStatusEnum
import javax.persistence.Embeddable
import javax.persistence.Entity

@Embeddable
class UserStatus(status: EbicsUserStatusEnum = EbicsUserStatusEnum.CREATED) : EbicsUserStatus(status)