package domain;

import java.time.LocalDate;

public class Income extends Transaction {

    public Income(LocalDate date, Double amount, String categoryCode, String note) {
        super(date, amount, categoryCode, note);
    }
}
