package com.wallet.transactionservice.infrastructure.inbound.web

import com.wallet.transactionservice.dtos.web.request.CreateTransactionRequest
import com.wallet.transactionservice.dtos.web.response.CreateTransactionResponse
import com.wallet.transactionservice.application.services.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/wallet-app/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {
    @PostMapping
    fun createTransaction(@RequestBody request: CreateTransactionRequest): ResponseEntity<CreateTransactionResponse> {
        return ResponseEntity(CreateTransactionResponse.from(transactionService.createTransaction(request)),
            HttpStatus.CREATED)
    }

}