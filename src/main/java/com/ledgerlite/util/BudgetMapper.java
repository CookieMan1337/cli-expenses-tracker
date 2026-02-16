package com.ledgerlite.util;

import com.ledgerlite.domain.Budget;
import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Money;
import com.ledgerlite.exception.NotFoundException;
import com.ledgerlite.persistence.InMemoryCategoryRepository;

import java.time.YearMonth;

public class BudgetMapper {
    private final InMemoryCategoryRepository categoryStorage;
    private final String defaultCurrency;

    public BudgetMapper(InMemoryCategoryRepository categoryStorage,
                        String defaultCurrency) {
        this.categoryStorage = categoryStorage;
        this.defaultCurrency = defaultCurrency;
    }

    public Budget createBudget(String args) {
        String[] parts = args.split(" ", 3);

        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Неверный формат. Используйте: ДАТА КАТЕГОРИЯ ЛИМИТ"
            );
        }

        try {
            YearMonth period = YearMonth.parse(parts[0]);
            String categoryCode = parts[1];
            Category category = categoryStorage.findById(categoryCode)
                    .orElseThrow(() -> new NotFoundException("Категория бюджета не найдена"));

            String limit = parts[2];
            Money limitMoney = Money.of(limit, defaultCurrency);

            return new Budget(period, category, limitMoney);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Ошибка при формировании бюджета: " + exception.getMessage(), exception);
        }
    }
}
