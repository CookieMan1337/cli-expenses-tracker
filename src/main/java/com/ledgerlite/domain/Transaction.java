package main.java.com.ledgerlite.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public abstract class Transaction {
    private final UUID id;
    private final LocalDate date;
    private final Money amount;
    private final Category category;
    private final String note;


    protected Transaction(UUID id, LocalDate date, Money amount, Category category, String note) {
        this.id = Objects.requireNonNull(id,"ID не может быть пустым");
        this.date = Objects.requireNonNull(date,"date не может быть пустым");
        this.amount = Objects.requireNonNull(amount,"amount не может быть пустым");
        this.category = Objects.requireNonNull(category,"category не может быть пустым");
        this.note = note;

        validate();
    }

    protected Transaction(LocalDate date, Money amount, Category category, String note){
        this(UUID.randomUUID(),date,amount,category,note);
    }

    private void validate(){
        if (date.isAfter(LocalDate.now())){
            throw new IllegalArgumentException("Дата не может быть в будущем!");
        }
        if (amount.value().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Сумма транзакции не может быть нулевой!");
        }
    }

    //Getters
    public UUID getId() {return id;}
    public LocalDate getDate() {return date;}
    public Money getAmount() {return amount;}
    public Category getCategory() {return category;}
    public String getNote() {return note;}

    //Абстрактный метод для определения типа транзакции
    public abstract TransactionType getType();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Transaction)) return false;
        Transaction that = (Transaction) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", date=" + date +
                ", amount=" + amount +
                ", category=" + category +
                ", note='" + note + '\'' +
                '}';
    }

    public enum TransactionType {
        INCOME, EXPENSE
    }
}
