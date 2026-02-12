package com.ledgerlite.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Income extends Transaction implements Serializable {
    public Income(LocalDate date, Money amount, Category category, String note) {
        super(date, amount, category, note);
    }
}