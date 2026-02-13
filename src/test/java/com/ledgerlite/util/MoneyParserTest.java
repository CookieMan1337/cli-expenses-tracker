package com.ledgerlite.util;

import com.ledgerlite.domain.Money;
import com.ledgerlite.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyParserTest {

    @Test
    void shouldParseValidAmountAndCurrency() {
        Money money = MoneyParser.parse("100.50", "RUB");

        assertEquals(new BigDecimal("100.50"), money.value());
        assertEquals("RUB", money.currency().getCurrencyCode());
    }

    @Test
    void shouldParseWithDefaultCurrency() {
        Money money = MoneyParser.parse("100.50");

        assertEquals(new BigDecimal("100.50"), money.value());
        assertEquals("RUB", money.currency().getCurrencyCode());
    }

    @Test
    void shouldParseWithSpaces() {
        Money money = MoneyParser.parse("  100.50  ", "  RUB  ");

        assertEquals(new BigDecimal("100.50"), money.value());
    }

    @Test
    void shouldThrowWhenAmountIsInvalid() {
        assertThrows(ValidationException.class,
                () -> MoneyParser.parse("not-a-number", "RUB"));
    }

    @Test
    void shouldThrowWhenCurrencyIsInvalid() {
        assertThrows(ValidationException.class,
                () -> MoneyParser.parse("100.50", "INVALID"));
    }

    @Test
    void shouldThrowWhenAmountIsBlank() {
        assertThrows(ValidationException.class,
                () -> MoneyParser.parse("", "RUB"));
    }

    @Test
    void shouldThrowWhenCurrencyIsBlank() {
        assertThrows(ValidationException.class,
                () -> MoneyParser.parse("100.50", ""));
    }

    @Test
    void shouldParseLargeAmount() {
        Money money = MoneyParser.parse("999999.99", "USD");

        assertEquals(new BigDecimal("999999.99"), money.value());
    }

    @Test
    void shouldParseSmallAmount() {
        Money money = MoneyParser.parse("0.01", "EUR");

        assertEquals(new BigDecimal("0.01"), money.value());
    }
}