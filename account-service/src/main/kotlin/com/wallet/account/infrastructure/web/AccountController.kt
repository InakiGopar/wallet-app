package com.wallet.account.infrastructure.web

import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.dtos.web.response.AccountResponse
import com.wallet.account.dtos.web.request.CreateAccountRequest
import com.wallet.account.dtos.web.request.UpdateStatusRequest
import com.wallet.account.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("api/wallet-app/accounts")
class AccountController(
    private val accountService: AccountService
) {
    @PostMapping
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<AccountResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AccountResponse.from(accountService.createAccount(Currency.valueOf(request.currency))))
    }

    @GetMapping("/{accountId}")
     fun getAccount(@PathVariable accountId: UUID): AccountResponse {
        return AccountResponse.from(accountService.getAccount(AccountId(accountId)))
    }

    @PatchMapping("/{accountId}")
    fun updateStatus(@PathVariable accountId: UUID, @RequestBody request: UpdateStatusRequest): ResponseEntity<Void> {
        accountService.updateStatus(AccountId(accountId), AccountStatus.valueOf(request.status))
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}