package com.ledgerlite.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvIgnore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public abstract class Transaction implements Serializable {
    @JsonIgnore
    @CsvIgnore
    transient UUID id;
    LocalDate date;
    Money amount;
    Category category;
    String note;

    protected Transaction(LocalDate date, Money amount, Category category,String note){
        this.id = UUID.randomUUID();
        this.date = Objects.requireNonNull(date);
        this.amount = Objects.requireNonNull(amount);
        this.category = Objects.requireNonNull(category);
        this.note = (note == null)?"":note.strip();
    }

    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        // Если ID null (старые версии файлов), генерируем новый
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId(){return id;}
    public LocalDate getDate(){return date;}
    public Money getAmount(){return amount;}
    public Category getCategory(){return category;}
    public String getNote(){return note;}

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Transaction t)) return false;
        return id.equals(t.id);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

}
