package com.ledgerlite.util;

import com.ledgerlite.exception.ValidationException;

import java.math.BigDecimal;

public final class Validator {
    private Validator(){}

    public static void checkNotNull(Object value, String fieldName){
        if (value == null){
            throw new ValidationException(fieldName + " не может быть не задано");
        }
    }

    public static void checkNotBlank(String value, String fieldName){
        if (value == null || value.trim().isEmpty()){
            throw new ValidationException(fieldName + " не может быть пустым");
        }
    }

    public static void checkPositive(BigDecimal value, String fieldName){
        if(value.compareTo(BigDecimal.ZERO) <= 0){
            throw new ValidationException(fieldName + " должно быть положительным");
        }
    }

    public static void checkPositive(double value, String fieldName){
        if(value <= 0){
            throw new ValidationException(fieldName + " должно быть положительным");
        }
    }

    public static void checkNotNegative(BigDecimal value, String fieldName){
        if(value.compareTo(BigDecimal.ZERO) < 0){
            throw new ValidationException(fieldName + " не может быть отрицательным");
        }
    }

    public static void checkNotNegative(double value, String fieldName){
        if(value < 0){
            throw new ValidationException(fieldName + " не может быть отрицательным");
        }
    }
}
