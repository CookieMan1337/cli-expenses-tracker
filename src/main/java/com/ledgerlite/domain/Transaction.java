package com.ledgerlite.domain;
/**
 * «шаблон» для любой операции
 * она всегда либо доход, либо расход
 * поэтому класс abstract
 **/
import java.time.LocalDate;
import java.util.UUID;

public abstract class Transaction {
    // Все поля приват, чтобы защитить изменения извне
    // и final чтобы после присвоения данные не изменялись

    private final UUID id;
    private final LocalDate date;
    private final Money amount;
    private final Category category;
    private final String note;

    // protected - конструктор доступен только внутри текущего пакета и классам-наследникам
    protected Transaction(LocalDate date, Money amount, Category category, String note) {
        this.id = UUID.randomUUID(); // Сами генерируем случайный ID
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.note = note;
    }

    // Геттеры
    public UUID getId() { return id; }
    public LocalDate getDate() { return date; }
    public Money getAmount() { return amount; }
    public Category getCategory() { return category; }
    public String getNote() { return note; }
}

