package com.ledgerlite.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Expense extends Transaction implements Serializable {
    public Expense(LocalDate date, Money amount, Category category, String note) {
        super(date, amount, category, note);
    }
}