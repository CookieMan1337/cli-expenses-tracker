package domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;


public abstract class Transaction {
    private final String id;
    private final LocalDate date;
    private final String note;
    private final Double amount;
    private final String categoryCode;

    public Transaction(LocalDate date, Double amount, String categoryCode, String note) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.amount = amount;
        this.categoryCode = categoryCode;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public Double getAmount() {
        return amount;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id) && Objects.equals(date, that.date) && Objects.equals(note, that.note)
                && Objects.equals(amount, that.amount) && Objects.equals(categoryCode, that.categoryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, note, amount, categoryCode);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", note='" + note + '\'' +
                ", amount=" + amount +
                ", categoryCode='" + categoryCode + '\'' +
                '}';
    }
}
