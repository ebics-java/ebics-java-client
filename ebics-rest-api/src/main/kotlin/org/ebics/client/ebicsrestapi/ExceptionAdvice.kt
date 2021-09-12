package org.ebics.client.ebicsrestapi

import org.ebics.client.api.AlreadyExistException
import org.ebics.client.api.FunctionException
import org.ebics.client.api.NotFoundException
import org.ebics.client.api.user.UserService
import org.ebics.client.exception.EbicsException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionAdvice {
    @ExceptionHandler( value =  [NotFoundException::class, AlreadyExistException::class, FunctionException::class,
        IllegalAccessException::class, AccessDeniedException::class, Exception::class])
    fun exceptionHandler(ex: Exception): ResponseEntity<ErrorMessage> {
        logger.error("Exception occur", ex)
        val status:HttpStatus = when(ex) {
            is NotFoundException -> HttpStatus.NOT_FOUND
            is AlreadyExistException -> HttpStatus.CONFLICT //EXISTING RESOURCE
            is FunctionException -> HttpStatus.BAD_REQUEST
            is EbicsException -> HttpStatus.BAD_REQUEST
            is IllegalAccessException -> HttpStatus.FORBIDDEN
            is AccessDeniedException -> HttpStatus.FORBIDDEN
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        return ResponseEntity<ErrorMessage>(
            ErrorMessage(httpStatus = status, message = ex.message ?: ex.toString(), description = ex.cause?.toString() ?: ex.toString()), status)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExceptionAdvice::class.java)
    }
}