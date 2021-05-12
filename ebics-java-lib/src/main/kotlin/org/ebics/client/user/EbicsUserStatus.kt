package org.ebics.client.user

class EbicsUserStatus(
    status: EbicsUserStatusEnum = EbicsUserStatusEnum.CREATED
) {
    var status: EbicsUserStatusEnum = status
        private set

    fun update(action: EbicsUserAction) {
        status = status.updateStatus(action)
    }

    fun check(action: EbicsUserAction) = status.checkAction(action)
}