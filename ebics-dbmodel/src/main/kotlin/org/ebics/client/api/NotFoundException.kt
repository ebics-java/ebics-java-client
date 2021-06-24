package org.ebics.client.api

import java.lang.RuntimeException

class NotFoundException(val id:Long, private val objectName: String, ex: Exception?) : RuntimeException("Can't find $objectName $id", ex)