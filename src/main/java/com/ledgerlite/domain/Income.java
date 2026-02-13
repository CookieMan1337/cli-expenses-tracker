package com.ledgerlite.domain;

import com.ledgerlite.exception.ValidationException;

import java.time.LocalDate;
import java.util.UUID;

public class Income extends Transaction{

    public Income(UUID id, LocalDate date, Money amount, Category category, String note) {
        super(id, date, amount, category, note);
        if (amount.isNegative()) {
            throw new ValidationException("Сумма дохода не может быть отрицательной");
        }
    }

    public Income(LocalDate date, Money amount, Category category, String note) {
        super(date, amount, category, note);
        if (amount.isNegative()) {
            throw new ValidationException("Сумма дохода не может быть отрицательной");
        }
    }

    Income() {
        super();
    }

    @Override
    public TransactionType getType() {
        return TransactionType.INCOME;
    }


}
