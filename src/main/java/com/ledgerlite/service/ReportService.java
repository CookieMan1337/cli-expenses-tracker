package com.ledgerlite.service;

import com.ledgerlite.domain.*;
import com.ledgerlite.persistence.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    private final Repository<Transaction, UUID> transactionRepository;
    private final Repository<Category, String> categoryRepository;

    public ReportService(Repository<Transaction, UUID> transactionRepository,
                         Repository<Category, String> categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public PeriodSummary getPeriodSummary(LocalDate from, LocalDate to) {
        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> !t.getDate().isBefore(from))
                .filter(t -> !t.getDate().isAfter(to))
                .collect(Collectors.toList());

        Money totalIncome = transactions.stream()
                .filter(t -> t instanceof Income)
                .map(Transaction::getAmount)
                .reduce(Money.ZERO_RUB, Money::add);

        Money totalExpense = transactions.stream()
                .filter(t -> t instanceof Expense)
                .map(Transaction::getAmount)
                .reduce(Money.ZERO_RUB, Money::add);

        Money balance = totalIncome.subtract(totalExpense);

        // Группировка расходов по категориям
        Map<Category, Money> expensesByCategory = transactions.stream()
                .filter(t -> t instanceof Expense)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(
                                Money.ZERO_RUB,
                                Transaction::getAmount,
                                Money::add
                        )
                ));
        // Сортируем категории по сумме расходов
        List<CategoryExpense> topCategories = expensesByCategory.entrySet().stream()
                .map(e -> new CategoryExpense(e.getKey(), e.getValue()))
                .sorted((c1, c2) -> c2.amount().compareTo(c1.amount()))
                .collect(Collectors.toList());

        return new PeriodSummary(
                from, to,
                totalIncome,
                totalExpense,
                balance,
                transactions.size(),
                topCategories
        );
    }
    //Сводка за текущий месяц
    public PeriodSummary getCurrentMonthSummary() {
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());
        return getPeriodSummary(firstDay, lastDay);
    }
    //Топ расходов
    public List<Transaction> getTopExpenses(int limit) {
        return transactionRepository.findAll().stream()
                .filter(t -> t instanceof Expense)
                .sorted((t1, t2) -> t2.getAmount().compareTo(t1.getAmount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public record PeriodSummary(
            LocalDate from,
            LocalDate to,
            Money totalIncome,
            Money totalExpense,
            Money balance,
            int transactionCount,
            List<CategoryExpense> topCategories
    ) {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📊 ОТЧЁТ: %s - %s%n", from, to));
            sb.append(String.format("   Доходы:  %s%n", totalIncome));
            sb.append(String.format("   Расходы: %s%n", totalExpense));
            sb.append(String.format("   Баланс:  %s%n", balance));

            if (!topCategories.isEmpty()) {
                sb.append(String.format("%n   РАСХОДЫ ПО КАТЕГОРИЯМ:%n"));
                for (CategoryExpense ce : topCategories) {
                    double percent = ce.amount().value()
                            .divide(totalExpense.value(), 4, java.math.RoundingMode.HALF_UP)
                            .doubleValue() * 100;
                    sb.append(String.format("   %s: %s (%.1f%%)%n",
                            ce.category().name(), ce.amount(), percent));
                }
            }

            return sb.toString();
        }
    }

    //Расходы по категории (внутренний record)

    public record CategoryExpense(
            Category category,
            Money amount
    ) {}
}