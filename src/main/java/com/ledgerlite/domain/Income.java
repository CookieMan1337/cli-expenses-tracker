package com.ledgerlite.domain;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Income extends Transaction {
    public Income(LocalDate date, Money amount, Category category, String note) {
        super(date, amount, category, note);
    }

    @Override
    public String getType() {
        return "INCOME";
    }
}