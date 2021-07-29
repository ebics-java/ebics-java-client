package org.ebics.client.api

import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatusEnum

interface EbicsUser : EbicsUserInfo {
    override val ebicsVersion: EbicsVersion
    override val userId: String
    override val name: String
    override val dn: String
    override var userStatus: EbicsUserStatusEnum
    override val securityMedium: String get() = "0000"
    override val useCertificate: Boolean

    companion object {
        /**
         * Makes the Distinguished Names for the user certificates.
         * @param name the user name
         * @param email the user email
         * @param country the user country
         * @param organization the user organization
         * @return
         */
        fun makeDN(
            name: String,
            email: String?,
            country: String?,
            organization: String?
        ): String {
            val buffer = StringBuilder()
            buffer.append("CN=").append(name)
            if (country != null) {
                buffer.append(", " + "C=").append(country.toUpperCase())
            }
            if (organization != null) {
                buffer.append(", " + "O=").append(organization)
            }
            if (email != null) {
                buffer.append(", " + "E=").append(email)
            }
            return buffer.toString()
        }
    }

    val partner: EbicsPartner
}

