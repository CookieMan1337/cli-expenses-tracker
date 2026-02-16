package com.ledgerlite.domain;

import java.time.YearMonth;

public record Budget(YearMonth period, Category category, Money limit) {
    public Budget {
        if (period == null) {
            throw new IllegalArgumentException("Период бюджета не может быть пустым");
        }

        if (category == null) {
            throw new IllegalArgumentException("Категорию бюджета не может быть пустой");
        }

        if (limit == null) {
            throw  new IllegalArgumentException("Лимит бюджета не может быть пустым");
        }
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s", period, category.code(), limit);
    }
}