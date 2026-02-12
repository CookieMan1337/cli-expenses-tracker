package com.ledgerlite.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal value, Currency currency) {

    public Money {
        Objects.requireNonNull(value, "value не может быть пустым");
        Objects.requireNonNull(currency, "currency не может быть пустым");
        if (value.compareTo(BigDecimal.ZERO)<0) {
            throw new IllegalArgumentException("value должно быть больше нуля");
        }

        value = value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static final Money ZERO_RUB = new Money(BigDecimal.ZERO, Currency.getInstance("RUB"));

    //Статические фабричные методы для удобства создания
    public static Money of(BigDecimal value, Currency currency) {
        return new Money(value, currency);
    }

    public static Money of(double value, Currency currency){
        return new Money(BigDecimal.valueOf(value),currency);
    }

    public static Money of(double value, String currencyCode){
        return new Money(BigDecimal.valueOf(value), Currency.getInstance(currencyCode));
    }

    public static Money zero(Currency currency){
        return new Money(BigDecimal.ZERO,currency);
    }

    //Арифметические операции
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.value.add(other.value),this.currency);
    }

    public Money subtract(Money other){
        validateSameCurrency(other);
        return new Money(this.value.subtract(other.value),this.currency);
    }

    public Money multiply(BigDecimal multyplier){
        return new Money(this.value.multiply(multyplier),this.currency);
    }
    public Money multiply(double multiplier){
        return multiply(BigDecimal.valueOf(multiplier));
    }

    //Сравнения
    public boolean isGreaterThan(Money other){
        validateSameCurrency(other);
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isLessThan(Money other){
        validateSameCurrency(other);
        return this.value.compareTo(other.value) < 0;
    }

    public int compareTo(Money other) {
        validateSameCurrency(other);
        return this.value.compareTo(other.value);
    }

    public boolean isNegative(){
        return this.value.compareTo(BigDecimal.ZERO) < 0;
    }
    //Валидация
    private void validateSameCurrency(Money other){
        if (!this.currency.equals(other.currency)){
            throw new IllegalArgumentException(
                    String.format("currency не сходятся: %s vs %s", this.currency, other.currency)
            );
        }
    }

    @Override
    public String toString() {
        return "Money{" +
                "value=" + value +
                ", currency=" + currency +
                '}';
    }
}
