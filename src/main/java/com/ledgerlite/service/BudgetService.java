package com.ledgerlite.service;

import com.ledgerlite.domain.*;
import com.ledgerlite.persistence.InMemoryBudgetRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BudgetService {
    private final InMemoryBudgetRepository budgetStorage;

    public BudgetService(InMemoryBudgetRepository budgetStorage) {
        this.budgetStorage = budgetStorage;
    }

    public Budget setBudget(Budget budget) {
        try {
            return budgetStorage.save(budget);
        } catch (Exception exception) {
            log.info("Ошибка при установке бюджета: {}", exception.getMessage());
            throw new IllegalArgumentException("Ошибка при установке бюджета: " + exception.getMessage(), exception);
        }
    }

    public List<Budget> getAllBudgets() {
        return budgetStorage.findAll();
    }

    public void deleteAllBudgets() {
        budgetStorage.deleteAll();
        log.info("Бюджеты удалены");
    }

    public Optional<Budget> findByPeriodAndCategory(YearMonth period, Category category) {
        return budgetStorage.findByPeriodAndCategory(period, category);
    }
}
