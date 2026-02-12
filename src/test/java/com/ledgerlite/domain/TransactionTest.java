package com.ledgerlite.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование абстрактного класса Transaction")
class TransactionTest {

    private LocalDate testDate;
    private Money testMoney;
    private Category testCategory;
    private String testNote;
    private TestTransaction transaction;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
        testMoney = new Money(new BigDecimal("1000.00"), Currency.getInstance("RUB"));
        testCategory = new Category("FOOD", "Еда");
        testNote = "Тестовая транзакция";
        transaction = new TestTransaction(testDate, testMoney, testCategory, testNote);
    }

    @Nested
    @DisplayName("Конструктор и валидация")
    class ConstructorTests {

        @Test
        @DisplayName("Успешное создание транзакции с валидными параметрами")
        void shouldCreateTransactionWithValidParameters() {
            assertNotNull(transaction);
            assertNotNull(transaction.getId());
            assertEquals(testDate, transaction.getDate());
            assertEquals(testMoney, transaction.getAmount());
            assertEquals(testCategory, transaction.getCategory());
            assertEquals(testNote, transaction.getNote());
        }

        @Test
        @DisplayName("Генерация UUID при создании")
        void shouldGenerateUniqueIds() {
            TestTransaction another = new TestTransaction(testDate, testMoney, testCategory, testNote);
            assertNotNull(transaction.getId());
            assertNotNull(another.getId());
            assertNotEquals(transaction.getId(), another.getId());
        }

        @Test
        @DisplayName("Обработка null заметки")
        void shouldHandleNullNote() {
            TestTransaction tx = new TestTransaction(testDate, testMoney, testCategory, null);
            assertEquals("", tx.getNote());
        }

        @Test
        @DisplayName("Обработка пустой заметки")
        void shouldHandleEmptyNote() {
            TestTransaction tx = new TestTransaction(testDate, testMoney, testCategory, "");
            assertEquals("", tx.getNote());
        }

        @Test
        @DisplayName("Обработка заметки с пробелами")
        void shouldHandleWhitespaceNote() {
            TestTransaction tx = new TestTransaction(testDate, testMoney, testCategory, "  test  ");
            assertEquals("test", tx.getNote());
        }

        @Test
        @DisplayName("Выброс исключения при null дате")
        void shouldThrowExceptionWhenDateIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new TestTransaction(null, testMoney, testCategory, testNote)
            );
        }

        @Test
        @DisplayName("Выброс исключения при null сумме")
        void shouldThrowExceptionWhenAmountIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new TestTransaction(testDate, null, testCategory, testNote)
            );
        }

        @Test
        @DisplayName("Выброс исключения при null категории")
        void shouldThrowExceptionWhenCategoryIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new TestTransaction(testDate, testMoney, null, testNote)
            );
        }
    }


    @Nested
    @DisplayName("Сериализация")
    class SerializationTests {

        @Test
        @DisplayName("Сериализация и десериализация транзакции")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            // Сериализация
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(transaction);
            oos.close();

            // Десериализация
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            TestTransaction deserialized = (TestTransaction) ois.readObject();

            // Проверки
            assertEquals(transaction.getDate(), deserialized.getDate());
            assertEquals(transaction.getAmount(), deserialized.getAmount());
            assertEquals(transaction.getCategory(), deserialized.getCategory());
            assertEquals(transaction.getNote(), deserialized.getNote());
        }

        @Test
        @DisplayName("Восстановление null ID при десериализации")
        void shouldRegenerateIdIfNull() throws IOException, ClassNotFoundException {
            // Создаем транзакцию с null ID
            TestTransaction txWithNullId = new TestTransaction(testDate, testMoney, testCategory, testNote);
            setId(txWithNullId, null);

            // Сериализация
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(txWithNullId);
            oos.close();

            // Десериализация
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            TestTransaction deserialized = (TestTransaction) ois.readObject();

            // Должен сгенерировать новый ID
            assertNotNull(deserialized.getId());
        }

        private void setId(TestTransaction tx, UUID id) {
            try {
                java.lang.reflect.Field field = Transaction.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(tx, id);
            } catch (Exception e) {
                fail("Не удалось установить ID: " + e.getMessage());
            }
        }
    }

    // Тестовый класс для абстрактного Transaction
    private static class TestTransaction extends Transaction {
        public TestTransaction(LocalDate date, Money amount, Category category, String note) {
            super(date, amount, category, note);
        }
    }
}
