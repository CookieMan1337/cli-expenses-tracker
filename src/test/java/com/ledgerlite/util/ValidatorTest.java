package com.ledgerlite.util;

import com.ledgerlite.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test
    void shouldNotThrowWhenObjectNotNull() {
        assertDoesNotThrow(() -> Validator.checkNotNull("test", "field"));
    }

    @Test
    void shouldThrowWhenObjectNull() {
        assertThrows(ValidationException.class,
                () -> Validator.checkNotNull(null, "field"));
    }

    @Test
    void shouldNotThrowWhenStringNotBlank() {
        assertDoesNotThrow(() -> Validator.checkNotBlank("test", "field"));
    }

    @Test
    void shouldThrowWhenStringIsNull() {
        assertThrows(ValidationException.class,
                () -> Validator.checkNotBlank(null, "field"));
    }

    @Test
    void shouldThrowWhenStringIsEmpty() {
        assertThrows(ValidationException.class,
                () -> Validator.checkNotBlank("", "field"));
    }

    @Test
    void shouldThrowWhenStringIsWhitespace() {
        assertThrows(ValidationException.class,
                () -> Validator.checkNotBlank("   ", "field"));
    }

    @Test
    void shouldNotThrowWhenValueIsPositive() {
        assertDoesNotThrow(() -> Validator.checkPositive(new BigDecimal("10.5"), "field"));
        assertDoesNotThrow(() -> Validator.checkPositive(10.5, "field"));
    }

    @Test
    void shouldThrowWhenValueIsZero() {
        assertThrows(ValidationException.class,
                () -> Validator.checkPositive(BigDecimal.ZERO, "field"));
    }

    @Test
    void shouldThrowWhenValueIsNegative() {
        assertThrows(ValidationException.class,
                () -> Validator.checkPositive(new BigDecimal("-10.5"), "field"));
    }

    @Test
    void shouldNotThrowWhenValueIsNotNegative() {
        assertDoesNotThrow(() -> Validator.checkNotNegative(new BigDecimal("10.5"), "field"));
        assertDoesNotThrow(() -> Validator.checkNotNegative(BigDecimal.ZERO, "field"));
    }

    @Test
    void shouldThrowWhenValueIsNegativeForNotNegativeCheck() {
        assertThrows(ValidationException.class,
                () -> Validator.checkNotNegative(new BigDecimal("-10.5"), "field"));
    }
}