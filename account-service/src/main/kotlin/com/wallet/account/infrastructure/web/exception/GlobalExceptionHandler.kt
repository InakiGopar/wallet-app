package com.wallet.account.infrastructure.web.exception

import com.wallet.account.infrastructure.web.dtos.response.ApiError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(error: IllegalArgumentException): ResponseEntity<ApiError> =
        ResponseEntity
            .badRequest()
            .body(ApiError(error.message ?: "Invalid request"))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(error: NoSuchElementException): ResponseEntity<ApiError> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiError(error.message ?: "Resource not found"))
}