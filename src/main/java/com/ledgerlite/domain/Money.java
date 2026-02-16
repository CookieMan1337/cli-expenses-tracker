package com.ledgerlite.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal value, Currency currency) {
    public Money(BigDecimal value, Currency currency) {
        if (value == null) {
            throw new IllegalArgumentException("Сумма value не должна быть пустой");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Валюта currency не должна быть пустой");
        }
        this.value = value.setScale(2, RoundingMode.HALF_EVEN);
        this.currency = currency;
    }


    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance(currencyCode));
    }

    @Override
    public String toString() {
        return "Money{" +
                "value=" + value +
                ", currency=" + currency.toString() +
                '}';
    }
}
