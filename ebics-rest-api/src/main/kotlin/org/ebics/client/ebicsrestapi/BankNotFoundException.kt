package org.ebics.client.ebicsrestapi

import org.ebics.client.api.bank.BankNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class BankNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler(BankNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun employeeNotFoundHandler(ex: BankNotFoundException): String = ex.message ?: ex.toString()
}