package com.ledgerlite.domain;

import com.ledgerlite.exception.ValidationException;

import java.time.LocalDate;
import java.util.UUID;

public class Expense extends Transaction{

    public Expense(UUID id, LocalDate date, Money amount, Category category, String note) {
        super(id, date, amount, category, note);
        if (amount.isNegative()) {
            throw new ValidationException("Сумма траты не может быть отрицательной");
        }
    }

    public Expense(LocalDate date, Money amount, Category category, String note) {
        super(date, amount, category, note);
        if (amount.isNegative()) {
            throw new ValidationException("Сумма траты не может быть отрицательной");
        }
    }

    Expense() {
        super();
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }


}
