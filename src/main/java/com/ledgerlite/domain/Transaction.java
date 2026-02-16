package com.ledgerlite.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Income.class, name = "INCOME"),
        @JsonSubTypes.Type(value = Expense.class, name = "EXPENSE")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Transaction {
    protected UUID id;
    protected LocalDate date;
    protected Money amount;
    protected Category category;
    protected String note;


    public Transaction(LocalDate date, Money amount, Category category, String note) {
        this.id = UUID.randomUUID();
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.note = note;
    }

    public abstract String getType();

    @Override
    public String toString() {
        return String.format("%s | %-7s | %-10s | %-8s | %s",
                date, getType(), amount, category.code(), note);
    }
}