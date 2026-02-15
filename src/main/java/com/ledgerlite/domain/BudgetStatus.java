package com.ledgerlite.domain;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BudgetStatus(
        Budget budget,
        Money spent,
        boolean isExceeded
) {
    public BudgetStatus(Budget budget, Money spent) {
        this(
                budget,
                spent,
                spent.value().compareTo(budget.limit().value()) > 0
        );
    }

    public static BudgetStatus noBudget(Category category, YearMonth period) {
        return new BudgetStatus(null, null, false);
    }

    public BigDecimal getRemaining() {
        if (budget == null) return BigDecimal.ZERO;
        return budget.limit().value().subtract(spent.value());
    }
}