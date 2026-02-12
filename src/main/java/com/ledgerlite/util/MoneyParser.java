package com.ledgerlite.util;

import com.ledgerlite.domain.Money;
import com.ledgerlite.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Currency;

public class MoneyParser {
    private MoneyParser(){}

    public static Money parse(String amountStr, String currencyCode){
        Validator.checkNotBlank(amountStr, "Сумма");
        Validator.checkNotBlank(currencyCode, "Валюта");

        try {
            BigDecimal amount = new BigDecimal(amountStr.trim());
            Currency currency = Currency.getInstance(currencyCode.trim().toUpperCase());

            return Money.of(amount,currency);
        } catch (NumberFormatException e){
            throw new ValidationException("Невалидная сумма: " + amountStr, e);
        } catch (IllegalArgumentException e){
            throw new ValidationException("Невалидная валюта: " + currencyCode, e);
        }

    }

    //Если без валюты, то всегда рубли
    public static Money parse(String amountStr){

        return parse(amountStr, "RUB");
    }
}
