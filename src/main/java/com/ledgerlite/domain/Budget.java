package com.ledgerlite.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ledgerlite.exception.ValidationException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

public record Budget(YearMonth period, Category category, Money limit) {
    public Budget {
        Objects.requireNonNull(period,"period не может быть пустым");
        Objects.requireNonNull(category,"category не может быть пустым");
        Objects.requireNonNull(limit,"limit не может быть пустым");
        if (limit.value().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(String.format("Сумма бюджета должна быть больше нуля."));
        }
    }

    //В качестве айди будет дата и категория, на один месяц и категорию только один бюджет
    @JsonIgnore
    public String getId() {
        return String.format("%s-%d-%02d",
                category.code(),
                period.getYear(),
                period.getMonthValue()
        );
    }

    //Функция для проверки превышения лимита
    public boolean isOverrun(Money spent) {
        return spent.isGreaterThan(limit);
    }

    //Процент использованного бюджета
    public double getUsagePercentage(Money spent) {
        if (limit.value().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spent.value()
                .divide(limit.value(), 4, java.math.RoundingMode.HALF_UP)
                .doubleValue() * 100;
    }

    //Остаток бюджета
    public Money getRemaining(Money spent) {
        return limit.subtract(spent);
    }

    public String toString() {
        return String.format("Бюджет [%s %s: %s]",
                period, category.name(), limit);
    }
}



