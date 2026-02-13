package com.ledgerlite.domain;

import java.util.Objects;

public record Category(String code, String name) {
    public Category {
        Objects.requireNonNull(code, "code не может быть пустым");
        Objects.requireNonNull(name, "name не может быть пустым");
        if (code.isBlank()){ throw new IllegalArgumentException("code не может быть пустым");};
        if (name.isBlank()){ throw new IllegalArgumentException("name не может быть пустым");};
        code = code.trim().toUpperCase();
        name = name.trim();
    }

    //Предопределенные категории
    public static final Category PRODUCT = new Category("PRODUCT","Продукты");
    public static final Category FOOD = new Category("FOOD","Кафе и рестораны");
    public static final Category TRANSPORT = new Category("TRANSPORT","Транспорт");
    public static final Category TRAVEL = new Category("TRAVEL","Путешествия");
    public static final Category SALARY = new Category("SALARY","Зарплата");
    public static final Category INCOME = new Category("INCOME","Входящие переводы");
    public static final Category ENTERTAINMENT = new Category("ENTERTAINMENT","Развлечения");
    public static final Category SHOPPING = new Category("SHOPPING","Шоппинг");
    public static final Category OTHER = new Category("OTHER","Другое");

    public static Category[] defaultCategories(){
        return new Category[]{PRODUCT,FOOD,TRANSPORT,TRAVEL,SALARY,INCOME,ENTERTAINMENT,SHOPPING,OTHER};
    }
}
