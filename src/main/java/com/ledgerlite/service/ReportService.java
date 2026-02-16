package com.ledgerlite.service;

import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Expense;
import com.ledgerlite.report.ExportReportItem;
import com.ledgerlite.persistence.InMemoryTransactionRepository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    private final InMemoryTransactionRepository transactionStorage;

    public ReportService(InMemoryTransactionRepository transactionStorage) {
        this.transactionStorage = transactionStorage;
    }

    public Map<Category, BigDecimal> getSummary(YearMonth period) {
        List<Expense> expensesInPeriod = transactionStorage.findAllExpensesByPeriod(period);
        return expensesInPeriod.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                e -> e.getAmount().value(),
                                BigDecimal::add))
                );
    }

    public List<Expense> topExpenses(int n) {
        return transactionStorage.findAllExpenses().stream()
                .sorted((a, b) -> b.getAmount().value().compareTo(a.getAmount().value()))
                .limit(n)
                .toList();
    }

    public List<ExportReportItem> getReportRowsForExport(YearMonth period) {
        return getSummary(period).entrySet().stream().
                map(entry ->
                        new ExportReportItem(period, entry.getKey().code(), entry.getValue()))
                .toList();
    }
}