package com.ledgerlite.domain;

import java.math.BigDecimal;
import java.util.Currency;
import com.ledgerlite.exception.LedgerException;

/**
 * Money — record для денег
 * @param value сумма
 * @param currency валюта
 */
public record Money(BigDecimal value, Currency currency) {

    public static final Money ZERO_RUB = new Money(BigDecimal.ZERO, Currency.getInstance("RUB"));


    /**
     * Создаем объект Money для рублей
     */
    public Money {
        if (value == null) {
            throw new LedgerException("Сумма не может быть null");
        }
        if (currency == null) {
            throw new LedgerException("Валюта не может быть null");
        }

    }

    /**
     * Создаем объект Money с проверкой кода валюты
     */
    public static Money of(double amount, String currencyCode) {
        // Проверка на null или пустоту
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new LedgerException("Код валюты не может быть пустым");
        }

        // Валидация кода валюты
        Currency validatedCurrency;
        try {
            validatedCurrency = Currency.getInstance(currencyCode.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невалидный код валюты: " + currencyCode);
        }

        // Оптимизация для 0 RUB
        BigDecimal val = BigDecimal.valueOf(amount);
        if (val.signum() == 0 && "RUB".equals(validatedCurrency.getCurrencyCode())) {
            return ZERO_RUB;
        }

        return new Money(val, validatedCurrency);
    }

}