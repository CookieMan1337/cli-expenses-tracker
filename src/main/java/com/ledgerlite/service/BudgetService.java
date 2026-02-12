package com.ledgerlite.service;

import com.ledgerlite.domain.*;
import com.ledgerlite.persistence.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BudgetService {
    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);
    private final Map<YearMonth,Map<Category,Budget>> budgets;
    private final LedgerService ledgerService;

    public BudgetService(Map<YearMonth,Map<Category,Budget>> budgets, LedgerService ledgerService){
        this.budgets = budgets;
        this.ledgerService = ledgerService;
    }

    public boolean isExceed(YearMonth period, String categoryCode){
        Budget budget = getBudgetByPeriodAndCategory(period,categoryCode);
        if (budget == null) return false;
        List<Expense> expenses = ledgerService.getExpensesByPeriod(period).stream()
                .filter(e -> e.getCategory().code().equals(categoryCode))
                .collect(Collectors.toList());
        Money totalSpent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(new Money(BigDecimal.ZERO, budget.limit().currency()), Money::add);
        boolean exceeded = totalSpent.value().compareTo(budget.limit().value()) > 0;
        if (exceeded){
            log.warn("Budget exceeded in category '{}' by period '{}'", categoryCode, period);
        }
        return exceeded;
    }

    public List<Budget> getBudgetByPeriod(YearMonth period){
        return budgets.getOrDefault(period,Map.of()).values().stream().toList();
    }

    private Budget getBudgetByPeriodAndCategory(YearMonth period, String categoryCode){
        return budgets.getOrDefault(period,Map.of()).values().stream()
                .filter(b -> b.period().equals(period))
                .filter(b -> b.category().code().equals(categoryCode))
                .findFirst()
                .orElse(null);
    }
}
