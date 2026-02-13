package com.ledgerlite.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void shouldCreateCategoryWithValidValues() {
        Category category = new Category("FOOD", "Продукты");

        assertEquals("FOOD", category.code());
        assertEquals("Продукты", category.name());
    }

    @Test
    void shouldConvertCodeToUpperCase() {
        Category category = new Category("food", "Продукты");

        assertEquals("FOOD", category.code());
    }

    @Test
    void shouldTrimWhitespace() {
        Category category = new Category("  food  ", "  Продукты  ");

        assertEquals("FOOD", category.code());
        assertEquals("Продукты", category.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    void shouldThrowExceptionWhenCodeIsBlank(String blankCode) {
        assertThrows(IllegalArgumentException.class,
                () -> new Category(blankCode, "Name"));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new Category("CODE", "  "));
    }

    @Test
    void shouldHaveDefaultCategories() {
        Category[] defaults = Category.defaultCategories();

        assertNotNull(defaults);
        assertTrue(defaults.length > 0);

        // Проверяем наличие основных категорий
        boolean hasFood = false;
        boolean hasSalary = false;

        for (Category cat : defaults) {
            if (cat.code().equals("FOOD")) hasFood = true;
            if (cat.code().equals("SALARY")) hasSalary = true;
        }

        assertTrue(hasFood);
        assertTrue(hasSalary);
    }

    @Test
    void testEqualsAndHashCode() {
        Category cat1 = new Category("FOOD", "Продукты");
        Category cat2 = new Category("FOOD", "Продукты");
        Category cat3 = new Category("TRANSP", "Транспорт");

        assertEquals(cat1, cat2);
        assertEquals(cat1.hashCode(), cat2.hashCode());
        assertNotEquals(cat1, cat3);
    }
}