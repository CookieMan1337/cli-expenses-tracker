package com.ledgerlite.persistence;

import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Expense;
import com.ledgerlite.domain.Transaction;

import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTransactionRepository implements Repository<Transaction> {
    private final Map<UUID, Transaction> storage = new ConcurrentHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Транзакция для добавления не указана");
        }
        storage.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id для поиска транзакции не может быть пустым");
        }
        UUID uuid = UUID.fromString(id);
        return Optional.ofNullable(storage.get(uuid));
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean delete(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id для удаления транзакции не может быть пустым");
        }
        UUID uuid = UUID.fromString(id);
        return storage.remove(uuid) != null;
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }

    public List<Expense> findAllExpenses() {
        return new ArrayList<>(storage.values()).stream()
                .filter(transaction -> transaction instanceof Expense)
                .map(t -> (Expense) t)
                .toList();
    }

    public List<Expense> findAllExpensesByPeriod(YearMonth period) {
        return new ArrayList<>(storage.values()).stream()
                .filter(transaction -> transaction instanceof Expense)
                .filter(t -> YearMonth.from(t.getDate()).equals(period))
                .map(t -> (Expense) t)
                .toList();
    }

    public List<Expense> findAllExpensesByPeriodAndCategory(YearMonth period, Category category) {
        return new ArrayList<>(storage.values()).stream()
                .filter(transaction -> transaction instanceof Expense)
                .filter(t -> t.getCategory().code().equals(category.code()))
                .filter(t -> YearMonth.from(t.getDate()).equals(period))
                .map(t -> (Expense) t)
                .toList();
    }
}