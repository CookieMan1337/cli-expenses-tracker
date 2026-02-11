package com.ledgerlite.domain;

import java.io.Serializable;
import java.util.UUID;

public record Category(String code, String name) implements Serializable {
    public Category{
        // UUID id = UUID.randomUUID();
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code is blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is blank");
    }
}
