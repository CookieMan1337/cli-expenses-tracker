package com.ledgerlite.domain;

import com.ledgerlite.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование Category")
class CategoryTest {

    @Test
    @DisplayName("Создание категории с валидными параметрами")
    void shouldCreateCategoryWithValidParameters() {
        Category category = new Category("FOOD", "Еда");

        assertEquals("FOOD", category.code());
        assertEquals("Еда", category.name());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("Выброс исключения при пустом коде")
    void shouldThrowExceptionWhenCodeIsBlank(String invalidCode) {
        assertThrows(IllegalArgumentException.class,
                () -> new Category(invalidCode, "Еда"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("Выброс исключения при пустом названии")
    void shouldThrowExceptionWhenNameIsBlank(String invalidName) {
        assertThrows(IllegalArgumentException.class,
                () -> new Category("FOOD", invalidName));
    }

    @Test
    @DisplayName("Категории с одинаковыми кодами не равны")
    void shouldNotBeEqualWithSameCode() {
        Category cat1 = new Category("FOOD", "Еда");
        Category cat2 = new Category("FOOD", "Продукты");

        assertNotEquals(cat1, cat2);
        assertNotEquals(cat1.hashCode(), cat2.hashCode());
    }

    @Test
    @DisplayName("Строковое представление категории")
    void shouldReturnStringRepresentation() {
        Category category = new Category("FOOD", "Еда");
        String expected = "Category[code=FOOD, name=Еда]";

        assertEquals(expected, category.toString());
    }
}