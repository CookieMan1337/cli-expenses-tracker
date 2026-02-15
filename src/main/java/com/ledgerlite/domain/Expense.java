package com.ledgerlite.domain;

import java.time.LocalDate;

//наследование от Transaction
public class Expense extends Transaction {
    public Expense(LocalDate date, Money amount, Category category, String note) {
        //конструктор род. класса
        super(date, amount, category, note);
    }
}
