package domain;

import java.time.LocalDate;

public class Expense extends Transaction {

    public Expense(LocalDate date, Double amount, String categoryCode, String note) {
        super(date, amount, categoryCode, note);
    }
}
