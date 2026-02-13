package com.ledgerlite.service;

import com.ledgerlite.domain.*;
import com.ledgerlite.exception.ValidationException;
import com.ledgerlite.persistence.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BudgetService {
    //Тут криво что мы рекорд Repository используем, потому что юид идет сборный
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

    //Получить все бюджеты
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    //Проверка превышения (для учета при добавлении трат)
    public boolean isBudgetExceeded(YearMonth period, Category category) {
        Optional<Budget> budgetOpt = getBudget(period, category);
        if (budgetOpt.isEmpty()) {
            return false; // Нет бюджета — нет превышения
        }

        Budget budget = budgetOpt.get();
        Money spent = calculateSpent(period, category);
        return spent.isGreaterThan(budget.limit());
    }

    //Получаем сумму трат в месяце
    public Money calculateSpent(YearMonth period, Category category) {
        return transactionRepository.findAll().stream()
                .filter(t -> t instanceof Expense)
                .filter(t -> t.getYearMonth().equals(period))
                .filter(t -> t.getCategory().equals(category))
                .map(Transaction::getAmount)
                .reduce(Money.ZERO_RUB, Money::add);
    }

    //Получить информацию о бюджете
    public BudgetInfo getBudgetInfo(YearMonth period, Category category) {
        Optional<Budget> budgetOpt = getBudget(period, category);
        if (budgetOpt.isEmpty()) {
            return new BudgetInfo(null, Money.ZERO_RUB, 0, false);
        }

        Budget budget = budgetOpt.get();
        Money spent = calculateSpent(period, category);
        //Процент использования бюджета, если больше 0, то считаем, иначе = 0
        double percent = budget.limit().value().doubleValue() > 0
                ? (spent.value().doubleValue() / budget.limit().value().doubleValue()) * 100
                : 0;
        boolean exceeded = spent.isGreaterThan(budget.limit());

        return new BudgetInfo(budget, spent, percent, exceeded);
    }

    //Простой рекорд внутри класса, чтоб выдать инфо по бюджету (это ок?)
    public record BudgetInfo(Budget budget, Money spent, double percentUsed, boolean exceeded) {
        public boolean hasBudget() {
            return budget != null;
        }
    }

    private String generateId(YearMonth period, Category category) {
        return category.code() + "-" + period.toString();
    }
}