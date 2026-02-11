package com.ledgerlite.domain;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.UUID;

public record Budget(YearMonth period, Category category, Money limit) implements Serializable {
    public Budget{
        // UUID id = UUID.randomUUID();
        if (period == null) throw new IllegalArgumentException("period is blank");
        if (category == null) throw new IllegalArgumentException("category is blank");
        if (limit == null) throw new IllegalArgumentException("limit is blank");
    }
}

