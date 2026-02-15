package com.ledgerlite.domain;

import java.time.YearMonth;
import java.util.UUID;

public record Budget(UUID id, YearMonth period, Category category, Money limit) {

    public static Budget of(YearMonth period, Category category, Money limit) {
        return new Budget(UUID.randomUUID(), period, category, limit);
    }
}
