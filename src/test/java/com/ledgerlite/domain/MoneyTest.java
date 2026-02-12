package com.ledgerlite.domain;

import com.ledgerlite.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {
    private final Currency RUB = Currency.getInstance("RUB");
    private final Money m1 = new Money(new BigDecimal("100.00"), RUB);
    private final Money m2 = new Money(new BigDecimal("50.50"), RUB);

    @Test
    void testAdd() {
        Money result = m1.add(m2);
        assertEquals(new BigDecimal("150.50"), result.value());
    }

    @Test
    void testSubtract() {
        Money result = m1.subtract(m2);
        assertEquals(new BigDecimal("49.50"), result.value());
    }

    @Test
    void testNegative() {
        assertTrue(m2.subtract(m1).isNegative());
    }
}