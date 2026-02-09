package test.java.com.ledgerlite.domain;

import main.java.com.ledgerlite.domain.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithValidValues() {
        // Given
        BigDecimal value = new BigDecimal("100.50");
        Currency currency = Currency.getInstance("RUB");

        // When
        Money money = new Money(value, currency);

        // Then
        assertEquals(new BigDecimal("100.50"), money.value());
        assertEquals(currency, money.currency());
    }

    @Test
    void shouldThrowExceptionWhenValueIsNegative() {
        // Given
        BigDecimal negativeValue = new BigDecimal("-10.00");
        Currency currency = Currency.getInstance("RUB");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Money(negativeValue, currency)
        );
        assertTrue(exception.getMessage().contains("value должно быть больше нуля"));
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        // When & Then
        assertThrows(
                NullPointerException.class,
                () -> new Money(null, Currency.getInstance("RUB"))
        );
    }

    @Test
    void shouldAddMoneyWithSameCurrency() {
        // Given
        Money money1 = Money.of(100.50, "RUB");
        Money money2 = Money.of(50.25, "RUB");

        // When
        Money result = money1.add(money2);

        // Then
        assertEquals(new BigDecimal("150.75"), result.value());
    }

    @Test
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        // Given
        Money rub = Money.of(100, "RUB");
        Money usd = Money.of(100, "USD");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rub.add(usd)
        );
        assertTrue(exception.getMessage().contains("currency не сходятся"));
    }

    @Test
    void testFactoryMethodOf() {
        // When
        Money money = Money.of(100.50, "RUB");

        // Then
        assertEquals(new BigDecimal("100.50"), money.value());
        assertEquals(Currency.getInstance("RUB"), money.currency());
    }

    @Test
    void testFactoryMethodZero() {
        // When
        Money zero = Money.zero(Currency.getInstance("RUB"));

        // Then
        assertEquals(BigDecimal.ZERO, zero.value());
    }

    @Test
    void testIsNegative() {
        // Given
        Money positive = Money.of(100, "RUB");
        Money zero = Money.zero(Currency.getInstance("RUB"));

        // Then
        assertFalse(positive.isNegative());
        assertFalse(zero.isNegative());
    }

    @Test
    void testSubtract() {
        // Given
        Money money1 = Money.of(100.50, "RUB");
        Money money2 = Money.of(50.25, "RUB");

        // When
        Money result = money1.subtract(money2);

        // Then
        assertEquals(new BigDecimal("50.25"), result.value());
    }

    @Test
    void testMultiply() {
        // Given
        Money money = Money.of(100, "RUB");

        // When
        Money result = money.multiply(2.5);

        // Then
        assertEquals(new BigDecimal("250.00"), result.value());
    }
}
