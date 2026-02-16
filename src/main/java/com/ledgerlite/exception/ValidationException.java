package com.ledgerlite.exception;

import java.math.BigDecimal;

/**
 * Ошибка валидации
 */
class ValidationException extends LedgerException {

    public ValidationException(String message) {
        super(message);
    }
    public static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Сумма должна быть больше нуля!");
        }
    }


}
