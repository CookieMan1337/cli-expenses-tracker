package com.ledgerlite.report;

import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Money;

public record CategoryExpense(
        Category category,
        Money amount
) {}