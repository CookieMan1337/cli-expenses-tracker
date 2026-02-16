package com.ledgerlite.service;


import com.ledgerlite.domain.*;
import com.ledgerlite.persistence.Repository;
import java.time.YearMonth;
import java.util.UUID;

public class BudgetService {
    private final Repository<Budget, UUID> budgetRepository;
    private final Repository<Transaction, UUID> transactionRepository;

    public BudgetService(Repository<Budget, UUID> budgetRepository, Repository<Transaction, UUID> transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    // Установка или обновление бюджета
    public void setBudget(Category category, YearMonth period, Money limit) {
        Budget budget = Budget.of(period, category, limit);
        budgetRepository.save(budget);
    }

}