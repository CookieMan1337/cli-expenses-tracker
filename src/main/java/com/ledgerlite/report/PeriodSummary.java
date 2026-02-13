package com.ledgerlite.report;

import com.ledgerlite.domain.Money;

import java.time.LocalDate;
import java.util.List;

public record PeriodSummary(
        LocalDate from,
        LocalDate to,
        Money totalIncome,
        Money totalExpense,
        Money balance,
        int transactionCount,
        List<CategoryExpense> topCategories
){}