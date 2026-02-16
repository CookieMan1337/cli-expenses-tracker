package com.ledgerlite.domain;

public record Category (String code, String name) {
    public Category(String code, String name) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Код категории должен быть не пустым");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя категории должно быть не пустым");
        }
        this.code = code.toUpperCase();
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
