/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.ebics.client.exception.h005

import org.ebics.client.exception.h004.EbicsServerException
import java.io.Serializable
import java.util.*

/**
 * Representation of EBICS return codes.
 * The return codes are described in chapter 13
 * of EBICS specification.
 *
 * @author hachani
 *
 * Constructs a new `ReturnCode` with a given
 * standard code, symbolic name and text
 * @param code the given standard code.
 * @param text the code text
 */
class EbicsReturnCode (
    val code: String,
    val text: String
) : Serializable {
    /**
     * Throws an equivalent `EbicsException`
     * @throws EbicsServerException
     */
    @Throws(EbicsServerException::class)
    fun throwException() {
        throw EbicsServerException(this, text)
    }

    /**
     * Tells if the return code is an OK one.
     * @return True if the return code is OK one.
     */
    val isOk: Boolean
        get() = code == EBICS_OK.code

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as EbicsReturnCode
        return code == that.code
    }

    override fun hashCode(): Int {
        return Objects.hash(code)
    }

    override fun toString(): String {
        return "$code $text"
    }

    companion object {
        /**
         * Returns the equivalent `ReturnCode` of a given code
         * @param code the given code
         * @param text the given code text
         * @return the equivalent `ReturnCode`
         */
        @JvmStatic
        fun toReturnCode(code: String, text: String): EbicsReturnCode {
            return EbicsReturnCode(code, text)
        }

        @JvmStatic
        fun toReturnCode(code: String): EbicsReturnCode {
            return returnCodes.getOrDefault(
                code, EbicsReturnCode(
                    code,
                    String.format("Unknown EBICS error %s", code)
                )
            )
        }
        private val returnCodes: MutableMap<String, EbicsReturnCode> = HashMap()

        val EBICS_OK: EbicsReturnCode = create("000000", "EBICS_OK")
        @JvmField
        val EBICS_DOWNLOAD_POSTPROCESS_DONE = create("011000", "EBICS_DOWNLOAD_POSTPROCESS_DONE")
        val EBICS_DOWNLOAD_POSTPROCESS_SKIPPED = create("011001", "EBICS_DOWNLOAD_POSTPROCESS_SKIPPED")
        val EBICS_TX_SEGMENT_NUMBER_UNDERRUN = create("011101", "EBICS_TX_SEGMENT_NUMBER_UNDERRUN")
        val EBICS_ORDER_PARAMS_IGNORED = create("031003", "EBICS_ORDER_PARAMS_IGNORED")
        val EBICS_AUTHENTICATION_FAILED = create("061001", "EBICS_AUTHENTICATION_FAILED")
        val EBICS_INVALID_REQUEST = create("061002", "EBICS_INVALID_REQUEST")
        val EBICS_INTERNAL_ERROR = create("061099", "EBICS_INTERNAL_ERROR")
        val EBICS_TX_RECOVERY_SYNC = create("061101", "EBICS_TX_RECOVERY_SYNC")
        @JvmField
        val EBICS_NO_DOWNLOAD_DATA_AVAILABLE = create("090005", "EBICS_NO_DOWNLOAD_DATA_AVAILABLE")
        val EBICS_INVALID_USER_OR_USER_STATE = create("091002", "EBICS_INVALID_USER_OR_USER_STATE")
        val EBICS_USER_UNKNOWN = create("091003", "EBICS_USER_UNKNOWN")
        val EBICS_INVALID_USER_STATE = create("091004", "EBICS_INVALID_USER_STATE")
        val EBICS_INVALID_ORDER_IDENTIFIER = create("091005", "EBICS_INVALID_ORDER_IDENTIFIER")
        val EBICS_UNSUPPORTED_ORDER_IDENTIFIER = create("091006", "EBICS_UNSUPPORTED_ORDER_IDENTIFIER")
        val EBICS_DISTRIBUTED_SIGNATURE_AUTHORISATION_FAILED = create("091007", "EBICS_DISTRIBUTED_SIGNATURE_AUTHORISATION_FAILED")
        val EBICS_BANK_PUBKEY_UPDATE_REQUIRED = create("091008", "EBICS_BANK_PUBKEY_UPDATE_REQUIRED")
        val EBICS_SEGMENT_SIZE_EXCEEDED = create("091009", "EBICS_SEGMENT_SIZE_EXCEEDED")
        val EBICS_INVALID_XML = create("091010", "EBICS_INVALID_XML")
        val EBICS_INVALID_HOST_ID = create("091011", "EBICS_INVALID_HOST_ID")
        val EBICS_TX_UNKNOWN_TXID = create("091101", "EBICS_TX_UNKNOWN_TXID")
        val EBICS_TX_ABORT = create("091102", "EBICS_TX_ABORT")
        val EBICS_TX_MESSAGE_REPLAY = create("091103", "EBICS_TX_MESSAGE_REPLAY")
        val EBICS_TX_SEGMENT_NUMBER_EXCEEDED = create("091104", "EBICS_TX_SEGMENT_NUMBER_EXCEEDED")
        val EBICS_INVALID_ORDER_PARAMS = create("091112", "EBICS_INVALID_ORDER_PARAMS")
        val EBICS_INVALID_REQUEST_CONTENT = create("091113", "EBICS_INVALID_REQUEST_CONTENT")
        val EBICS_MAX_ORDER_DATA_SIZE_EXCEEDED = create("091117", "EBICS_MAX_ORDER_DATA_SIZE_EXCEEDED")
        val EBICS_MAX_SEGMENTS_EXCEEDED = create("091118", "EBICS_MAX_SEGMENTS_EXCEEDED")
        val EBICS_MAX_TRANSACTIONS_EXCEEDED = create("091119", "EBICS_MAX_TRANSACTIONS_EXCEEDED")
        val EBICS_PARTNER_ID_MISMATCH = create("091120", "EBICS_PARTNER_ID_MISMATCH")
        val EBICS_ORDER_ALREADY_EXISTS = create("091122", "EBICS_ORDER_ALREADY_EXISTS")

        val EBICS_X509_CERTIFICATE_NOT_VALID_YET = create("091209", "EBICS_X509_CERTIFICATE_NOT_VALID_YET")
        val EBICS_SIGNATURE_VERIFICATION_FAILED = create("091301", "EBICS_SIGNATURE_VERIFICATION_FAILED")
        val EBICS_INVALID_ORDER_DATA_FORMAT = create("090004", "EBICS_INVALID_ORDER_DATA_FORMAT")

        private const val BUNDLE_NAME = "org.ebics.client.exception.messages"
        private const val serialVersionUID = -497883146384363199L
        private fun create(code: String, symbolicName: String): EbicsReturnCode {
            val returnCode = EbicsReturnCode(code, symbolicName)
            returnCodes[code] = returnCode
            return returnCode
        }
    }
}