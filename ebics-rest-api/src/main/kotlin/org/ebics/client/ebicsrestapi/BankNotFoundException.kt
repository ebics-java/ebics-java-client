package org.ebics.client.ebicsrestapi

import org.ebics.client.api.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionAdvice {
    @ExceptionHandler(NotFoundException::class)
    fun employeeNotFoundHandler(ex: NotFoundException): ResponseEntity<ErrorMessage> {
        val status:HttpStatus = when(ex) {
            is NotFoundException -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        return ResponseEntity<ErrorMessage>(
            ErrorMessage(httpStatus = status, message = ex.message ?: ex.toString(), description = ex.toString()), status)
    }
}