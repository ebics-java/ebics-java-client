package org.ebics.client.ebicsrestapi

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus
import java.util.*

data class ErrorMessage(
    val timestamp: Date = Date(),
    @JsonIgnore
    val httpStatus: HttpStatus,
    val status: Int = httpStatus.value(),
    val error: String = httpStatus.reasonPhrase,
    val message: String,
    val description: String,
)
