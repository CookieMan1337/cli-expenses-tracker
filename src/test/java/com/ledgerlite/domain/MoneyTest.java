package com.ledgerlite.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithValidValues() {
        Money money = Money.of(100.50, "RUB");

        assertEquals(new BigDecimal("100.50"), money.value());
        assertEquals(Currency.getInstance("RUB"), money.currency());
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Money(null, Currency.getInstance("RUB")));
    }

    @Test
    void shouldThrowExceptionWhenCurrencyIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Money(BigDecimal.TEN, null));
    }

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money money1 = Money.of(100.50, "RUB");
        Money money2 = Money.of(50.25, "RUB");

        Money result = money1.add(money2);

        assertEquals(new BigDecimal("150.75"), result.value());
    }

    @Test
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        Money rub = Money.of(100, "RUB");
        Money usd = Money.of(100, "USD");

        assertThrows(IllegalArgumentException.class, () -> rub.add(usd));
    }

    @Test
    void shouldSubtractMoney() {
        Money money1 = Money.of(100.50, "RUB");
        Money money2 = Money.of(50.25, "RUB");

        Money result = money1.subtract(money2);

        assertEquals(new BigDecimal("50.25"), result.value());
    }

    @Test
    void shouldMultiplyMoney() {
        Money money = Money.of(100, "RUB");

        Money result = money.multiply(2.5);

        assertEquals(new BigDecimal("250.00"), result.value());
    }

    @Test
    void shouldDetectNegativeMoney() {
        Money positive = Money.of(100, "RUB");
        Money zero = Money.zero(Currency.getInstance("RUB"));
        Money negative = Money.of(-50, "RUB");

        assertFalse(positive.isNegative());
        assertFalse(zero.isNegative());
        assertTrue(negative.isNegative());
    }

    @Test
    void shouldCompareMoney() {
        Money money1 = Money.of(100, "RUB");
        Money money2 = Money.of(50, "RUB");

        assertTrue(money1.isGreaterThan(money2));
        assertFalse(money2.isGreaterThan(money1));
        assertTrue(money2.isLessThan(money1));
    }

    @ParameterizedTest
    @CsvSource({
            "100.50, 200.75, 301.25",
            "0.01, 0.02, 0.03",
            "999.99, 0.01, 1000.00"
    })
    void shouldAddVariousAmounts(String val1, String val2, String expected) {
        Money m1 = Money.of(Double.parseDouble(val1), "RUB");
        Money m2 = Money.of(Double.parseDouble(val2), "RUB");

        Money result = m1.add(m2);

        assertEquals(new BigDecimal(expected), result.value());
    }
}