package org.ebics.client.api

import java.lang.RuntimeException

/**
 * Here are defined application specific exception which are then mapped to specific HTTP code
 * All other exceptions as the once bellow are reported as per standard spring behaviour as HTTP 500 when occured in REST calls
 */
class NotFoundException(val id:Long, private val objectName: String, ex: Exception?) : RuntimeException("Can't find $objectName id='$id'", ex)
class AlreadyExistException(val id:Long, private val objectName: String, ex: Exception?) : RuntimeException("$objectName id='$id' already exist", ex)
class FunctionException(message: String, ex: Exception?) : RuntimeException(message, ex)