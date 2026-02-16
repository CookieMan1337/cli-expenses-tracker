package com.ledgerlite.domain;

import java.util.UUID;

public record Category(UUID id, String code, String name) {

    public static Category of(String code, String name) {
        return new Category(UUID.randomUUID(), code, name);
    }
}
