package com.ledgerlite.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void shouldCreateIncomeWithValidValues() {
        LocalDate date = LocalDate.now();
        Money amount = Money.of(1000, "RUB");
        Category category = new Category("SALARY", "Зарплата");

        Income income = new Income(date, amount, category, "Тестовый доход");

        assertNotNull(income.getId());
        assertEquals(date, income.getDate());
        assertEquals(amount, income.getAmount());
        assertEquals(category, income.getCategory());
        assertEquals("Тестовый доход", income.getNote());
        assertEquals(Transaction.TransactionType.INCOME, income.getType());
    }

    @Test
    void shouldCreateExpenseWithValidValues() {
        LocalDate date = LocalDate.now();
        Money amount = Money.of(500, "RUB");
        Category category = new Category("FOOD", "Продукты");

        Expense expense = new Expense(date, amount, category, "Тестовый расход");

        assertNotNull(expense.getId());
        assertEquals(date, expense.getDate());
        assertEquals(amount, expense.getAmount());
        assertEquals(category, expense.getCategory());
        assertEquals("Тестовый расход", expense.getNote());
        assertEquals(Transaction.TransactionType.EXPENSE, expense.getType());
    }

    @Test
    void shouldThrowExceptionWhenDateInFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Money amount = Money.of(100, "RUB");
        Category category = new Category("TEST", "Тест");

        assertThrows(IllegalArgumentException.class,
                () -> new Income(futureDate, amount, category, ""));
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        LocalDate date = LocalDate.now();
        Money zero = Money.of(0, "RUB");
        Category category = new Category("TEST", "Тест");

        assertThrows(IllegalArgumentException.class,
                () -> new Expense(date, zero, category, ""));
    }

    @Test
    void shouldGenerateDifferentIdsForDifferentTransactions() {
        LocalDate date = LocalDate.now();
        Money amount = Money.of(100, "RUB");
        Category category = new Category("TEST", "Тест");

        Income income1 = new Income(date, amount, category, "");
        Income income2 = new Income(date, amount, category, "");

        assertNotEquals(income1.getId(), income2.getId());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        Money amount = Money.of(100, "RUB");
        Category category = new Category("TEST", "Тест");

        Income income1 = new Income(id, date, amount, category, "Note 1");
        Income income2 = new Income(id, date, amount, category, "Note 2");
        Income income3 = new Income(date, amount, category, "Note 3");

        assertEquals(income1, income2); // Same ID
        assertEquals(income1.hashCode(), income2.hashCode());
        assertNotEquals(income1, income3); // Different IDs
        assertNotEquals(income1, new Object());
    }

    @Test
    void shouldGetYearMonthFromDate() {
        LocalDate date = LocalDate.of(2026, 2, 12);
        Money amount = Money.of(100, "RUB");
        Category category = new Category("TEST", "Тест");

        Income income = new Income(date, amount, category, "");

        assertEquals(2026, income.getYearMonth().getYear());
        assertEquals(2, income.getYearMonth().getMonthValue());
    }
}