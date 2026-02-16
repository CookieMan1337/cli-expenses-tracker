package com.ledgerlite.domain;

import java.time.LocalDate; /**
 * extends - наследование, Income получает все поля от Transaction
 */
public class Income extends Transaction {
    public Income(LocalDate date, Money amount, Category category, String note) {
        // super() — вызывает конструктор родительского класса Transaction
        super(date, amount, category, note);
    }
}
