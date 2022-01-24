package org.ebics.client.exception.h005

import org.ebics.client.exception.EbicsException

open class EbicsServerException(val ebicsReturnCode: EbicsReturnCode, message: String = ebicsReturnCode.toString()) : EbicsException( message )