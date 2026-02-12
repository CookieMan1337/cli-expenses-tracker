package com.ledgerlite.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

public record Money(BigDecimal value, Currency currency)implements Serializable {
    private static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);
    public Money{
        if (value == null) throw new IllegalArgumentException("value is blank");
        if (currency == null) throw new IllegalArgumentException("currency is blank");
        if (!Objects.equals(currency.getCurrencyCode(), "RUB")) throw new IllegalArgumentException("Invalid currency");
        value = value.setScale(2,RoundingMode.HALF_UP);
    }

    public Money add(Money other){
        return new Money(value.add(other.value, MATH_CONTEXT), this.currency);
    }

    public Money subtract(Money other){
        return new Money(value.subtract(other.value, MATH_CONTEXT), this.currency);
    }

    public static Money parse(String amount){
        Currency cur = defaultCurrency();
        BigDecimal val = new BigDecimal(amount);
        return new Money(val,cur);
    }

    private static Currency defaultCurrency(){
        try{
            return Currency.getInstance(Locale.getDefault());
        } catch (Exception e){
            return Currency.getInstance("RUB");
        }
    }

    @JsonIgnore
    public boolean isNegative(){
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    public Money negate(){
        return new Money(this.value.negate(), this.currency);
    }

    @Override
    public String toString(){
        return String.format("%.2f %s", value, currency);
    }

}
