package com.ledgerlite.util;

import com.ledgerlite.domain.*;
import com.ledgerlite.exception.NotFoundException;
import com.ledgerlite.persistence.InMemoryCategoryRepository;

import java.time.LocalDate;

public class TransactionMapper {
    private final InMemoryCategoryRepository categoryStorage;
    private final String defaultCurrency;

    public TransactionMapper(InMemoryCategoryRepository categoryRepository, String defaultCurrency) {
        this.categoryStorage = categoryRepository;
        this.defaultCurrency = defaultCurrency;
    }

    public Income createIncome(String args) {
        String[] parts = args.split(" ", 4);

        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Неверный формат. Используйте: ДАТА СУММА КАТЕГОРИЯ [ПРИМЕЧАНИЕ]"
            );
        }
        LocalDate date = LocalDate.parse(parts[0]);
        String amount = parts[1];
        String categoryCode = parts[2];
        String note = parts.length > 3 ? parts[3] : "";
        Category category = categoryStorage.findById(categoryCode)
                .orElseThrow(() -> new NotFoundException("Категория дохода не найдена"));
        Money money = Money.of(amount, defaultCurrency);
        return new Income(date, money, category, note);
    }

    public Expense createExpense(String args) {
        String[] parts = args.split(" ", 4);

        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Неверный формат. Используйте: ДАТА СУММА КАТЕГОРИЯ [ПРИМЕЧАНИЕ]"
            );
        }
        LocalDate date = LocalDate.parse(parts[0]);
        String amount = parts[1];
        String categoryCode = parts[2];
        String note = parts.length > 3 ? parts[3] : "";
        Category category = categoryStorage.findById(categoryCode)
                .orElseThrow(() -> new NotFoundException("Категория расхода не найдена"));
        Money money = Money.of(amount, defaultCurrency);
        return new Expense(date, money, category, note);
    }
}
