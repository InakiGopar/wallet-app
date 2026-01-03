package com.wallet.account.infrastructure.web.exception


import com.wallet.account.domian.exceptions.AccountNotFoundException
import com.wallet.account.domian.exceptions.InvalidAccountStateException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.zalando.problem.Problem
import org.zalando.problem.Status
import org.zalando.problem.spring.web.advice.ProblemHandling
import java.net.URI

@ControllerAdvice
class ExceptionTranslator : ProblemHandling {

    @ExceptionHandler(AccountNotFoundException::class)
    fun handleAccountNotFound(ex: AccountNotFoundException): Problem =
        Problem.builder()
            .withType(URI.create("urn:wallet:problem:account-not-found"))
            .withTitle("Account not found")
            .withStatus(Status.NOT_FOUND)
            .withDetail(ex.message)
            .build()

    @ExceptionHandler(InvalidAccountStateException::class)
    fun handleInvalidAccountState(ex: InvalidAccountStateException): Problem =
        Problem.builder()
            .withType(URI.create("urn:wallet:problem:invalid-account-state"))
            .withTitle("Invalid account state")
            .withStatus(Status.CONFLICT)
            .withDetail(ex.message)
            .build()

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): Problem =
        Problem.builder()
            .withType(URI.create("urn:wallet:problem:invalid-request"))
            .withTitle("Invalid request")
            .withStatus(Status.BAD_REQUEST)
            .withDetail(ex.message)
            .build()
}