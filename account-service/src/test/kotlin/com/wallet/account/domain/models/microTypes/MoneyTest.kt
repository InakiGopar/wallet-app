package com.wallet.account.domain.models.microTypes

import com.wallet.account.domian.exceptions.NegativeMoneyException
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class MoneyTest {
    @Test
    fun `should create money with positive amount`() {
        val money = Money(
            amount = BigDecimal("100"),
            currency = Currency.USD
        )


        assertEquals(BigDecimal("100"), money.amount)
        assertEquals(Currency.USD, money.currency)
    }

    @Test
    fun `should throw exception when amount is negative`() {
        assertThrows<NegativeMoneyException> {
            Money(
                amount = BigDecimal("-10"),
                currency = Currency.USD
            )
        }
    }
}