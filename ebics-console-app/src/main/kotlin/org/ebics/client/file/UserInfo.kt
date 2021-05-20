package org.ebics.client.file

import org.ebics.client.model.user.EbicsUserStatus
import org.ebics.client.model.EbicsVersion
import org.ebics.client.api.EbicsUserInfo
import org.ebics.client.api.EbicsUserInfo.Companion.makeDN
import java.io.IOException
import java.security.GeneralSecurityException

class UserInfo(
    override val ebicsVersion: EbicsVersion,
    override val userId: String,
    override val name: String,
    override val dn: String,
    override val userStatus: EbicsUserStatus = EbicsUserStatus()
) : EbicsUserInfo {

    /**
     *
     * @param ebicsVersion version of EBICS to be used.
     * @param userId UserId as obtained from the bank.
     * @param name the user name,
     * @param email the user email
     * @param country the user country
     * @param organization the user organization or company
     * @param passwordCallback a callback-handler that supplies us with the password.
     * This parameter can be null, in this case no password is used.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    constructor(
        ebicsVersion: EbicsVersion,
        userId: String,
        name: String,
        email: String?,
        country: String?,
        organization: String?,
        userStatus: EbicsUserStatus = EbicsUserStatus()
    ) : this(ebicsVersion, userId, name, makeDN(name, email, country, organization), userStatus)
}