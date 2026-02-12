package com.ledgerlite.service;

import com.ledgerlite.domain.Budget;
import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Expense;
import com.ledgerlite.domain.Money;
import com.ledgerlite.domain.Transaction;
import com.ledgerlite.exception.ValidationException;
import com.ledgerlite.persistence.Repository;

import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BudgetService {
    private final Repository<Budget, String> budgetRepository;
    private final Repository<Transaction, UUID> transactionRepository;

    public BudgetService(Repository<Budget, String> budgetRepository,
                         Repository<Transaction, UUID> transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    //Установка бюджета на месяц и категорию
    public Budget setBudget(YearMonth period, Category category, Money limit) {
        String id = generateId(period, category);
        //Проверка на уже существование такого Бюджета на категорию и месяц
        if (budgetRepository.findById(id).isPresent()) {
            throw new ValidationException(
                    String.format("Бюджет на %s для категории '%s' уже существует.",
                            period, category.name())
            );
        }
        Budget budget = new Budget(period, category, limit);
        return budgetRepository.save(budget);
    }

    public Optional<Budget> getBudget(YearMonth period, Category category) {
        String id = generateId(period, category);
        return budgetRepository.findById(id);
    }

    //Получаем сумму трат в месяце
    public Money getSpentAmount(YearMonth period, Category category) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getYearMonth().equals(period))
                .filter(t -> t.getCategory().equals(category))
                .filter(t -> t instanceof Expense)
                .map(Transaction::getAmount)
                .reduce(Money.ZERO_RUB, Money::add);
    }

    private String generateId(YearMonth period, Category category) {
        return category.code() + "-" + period.toString();
    }
}