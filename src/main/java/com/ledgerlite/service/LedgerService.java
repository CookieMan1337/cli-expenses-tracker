package com.ledgerlite.service;

import com.ledgerlite.domain.*;
import com.ledgerlite.persistence.InMemoryCategoryRepository;
import com.ledgerlite.persistence.InMemoryTransactionRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.util.List;

@Slf4j
public class LedgerService {
    private final InMemoryTransactionRepository transactionStorage;
    private final InMemoryCategoryRepository categoryStorage;

    public LedgerService(InMemoryTransactionRepository transactionStorage,
                         InMemoryCategoryRepository categoryStorage) {
        this.transactionStorage = transactionStorage;
        this.categoryStorage = categoryStorage;
    }

    public Category addCategory(String code, String name) {
        if (categoryStorage.findById(code).isPresent()) {
            log.error("Ошибка во время добавления категории. Категория {} уже существует", code);
            throw new IllegalArgumentException("Категория уже существует: " + code);
        }
        try {
            Category newCategory = new Category(code, name);
            categoryStorage.save(newCategory);
            log.info("Категория {} добавлена", code);
            return newCategory;
        } catch (Exception exception) {
            log.error("Ошибка во время создания категории: {}", exception.getMessage());
            throw new IllegalArgumentException("Ошибка при создании категории: " + exception.getMessage(), exception);
        }
    }

    public List<Category> getAllCategories() {
        return categoryStorage.findAll();
    }

    public void deleteCategories() {
        categoryStorage.deleteAll();
        log.info("Категории очищены");
    }

    public Income addIncome(Income income) {
        try {
            return (Income) transactionStorage.save(income);
        } catch (Exception exception) {
            log.error("Ошибка при создании дохода: {}", exception.getMessage());
            throw new IllegalArgumentException("Ошибка при создании дохода: " + exception.getMessage(), exception);
        }
    }

    public Expense addExpense(Expense expense) {
        try {
            return (Expense) transactionStorage.save(expense);
        } catch (Exception exception) {
            log.error("Ошибка при создании расхода: {}", exception.getMessage());
            throw new IllegalArgumentException("Ошибка при создании расхода: " + exception.getMessage(), exception);
        }
    }

    public List<Transaction> getAllTransactions() {
        return transactionStorage.findAll();
    }

    public List<Expense> findAllExpensesByPeriodAndCategory(YearMonth period, Category category) {
        return transactionStorage.findAllExpensesByPeriodAndCategory(period, category);
    }

    public void deleteTransactions() {
        transactionStorage.deleteAll();
        log.info("Транзакции очищены");
    }

    public boolean deleteTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Транзакция для удаления не найдена");
        }
        String transactionId = transaction.getId().toString();
        return transactionStorage.delete(transactionId);
    }
}
