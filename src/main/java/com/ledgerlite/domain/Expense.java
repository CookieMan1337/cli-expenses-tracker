package com.ledgerlite.domain;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Expense extends Transaction {
    public Expense(LocalDate date, Money amount, Category category, String note) {
        super(date, amount, category, note);
    }

    @Override
    public String getType() {
        return "EXPENSE";
    }
}
