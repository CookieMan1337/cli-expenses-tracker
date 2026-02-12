package com.ledgerlite.persistence;

import com.ledgerlite.domain.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileStoreTest {

    private FileStore fileStore;
    private Path projectDataDir;

    @BeforeEach
    void setup() {
        fileStore = new FileStore();

        projectDataDir = Paths.get("").toAbsolutePath().resolve("data");

        System.out.println("=== Тестирование FileStore ===");
        System.out.println("Проект: " + Paths.get("").toAbsolutePath());
        System.out.println("Папка data проекта: " + projectDataDir);
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            Path ledgerFile = projectDataDir.resolve("transactions.dat");
            if (Files.exists(ledgerFile)) {
                // Делаем backup вместо удаления
                Path backup = projectDataDir.resolve("transactions.backup." +
                        System.currentTimeMillis() + ".dat");
                Files.move(ledgerFile, backup, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Создан backup: " + backup.getFileName());
            }
        } catch (IOException e) {
            System.err.println("Не удалось очистить тестовые данные: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Основной функционал: сохранение и загрузка")
    void basicSaveLoadTest() throws Exception {
        Currency rub = Currency.getInstance("RUB");
        Category food = new Category("FOOD", "Food");

        Transaction expense = new Expense(
                LocalDate.of(2024, 3, 21),
                new Money(new BigDecimal("1500.50"), rub),
                food,
                "Обед в кафе"
        );

        List<Transaction> transactions = Collections.singletonList(expense);
        fileStore.saveTransactions(transactions);

        Path expectedFile = projectDataDir.resolve("transactions.dat");
        assertTrue(Files.exists(expectedFile),
                "Файл должен быть создан в проекте: " + expectedFile);

        System.out.println("Файл создан: " + expectedFile);
        System.out.println("Размер файла: " + Files.size(expectedFile) + " байт");

        List<Transaction> loaded = fileStore.loadTransactions();

        assertEquals(1, loaded.size(), "Должна быть загружена 1 транзакция");
        assertEquals("FOOD", loaded.get(0).getCategory().code());
        assertEquals("Обед в кафе", loaded.get(0).getNote());

        assertNotNull(loaded.get(0).getId(), "ID не должен быть null после загрузки");

        System.out.println("✓ Основной функционал работает");
    }

    @Test
    @DisplayName("Загрузка когда файла нет (первый запуск)")
    void loadWhenFileNotExistsTest() throws Exception {
        Path ledgerFile = projectDataDir.resolve("transactions.dat");
        Files.deleteIfExists(ledgerFile);

        List<Transaction> loaded = fileStore.loadTransactions();

        assertNotNull(loaded, "Результат не должен быть null");
        assertTrue(loaded.isEmpty(), "При отсутствии файла должен быть пустой список");

        System.out.println("✓ Загрузка из несуществующего файла работает");
    }

    @Test
    @DisplayName("Удаление транзакции из файла")
    void deleteTransactionTest() throws Exception {
        Currency rub = Currency.getInstance("RUB");
        Category cat1 = new Category("CAT1", "Category 1");
        Category cat2 = new Category("CAT2", "Category 2");

        Transaction t1 = new Expense(
                LocalDate.now(),
                new Money(new BigDecimal("500"), rub),
                cat1,
                "Первый расход"
        );
        Transaction t2 = new Expense(
                LocalDate.now().plusDays(1),
                new Money(new BigDecimal("1000"), rub),
                cat2,
                "Второй расход"
        );

        List<Transaction> transactions = Arrays.asList(t1, t2);
        fileStore.saveTransactions(transactions);

        List<Transaction> beforeDelete = fileStore.loadTransactions();
        assertEquals(2, beforeDelete.size(), "Должно быть 2 транзакции до удаления");

        assertNotNull(beforeDelete.get(0).getId(), "ID первой транзакции не должен быть null");
        assertNotNull(beforeDelete.get(1).getId(), "ID второй транзакции не должен быть null");

        System.out.println("ID для удаления: " + beforeDelete.get(0).getId());

        fileStore.deleteTransaction(beforeDelete.get(0).getId(), beforeDelete);

        List<Transaction> afterDelete = fileStore.loadTransactions();

        assertEquals(1, afterDelete.size(), "Должна остаться 1 транзакция");
        assertEquals("CAT2", afterDelete.get(0).getCategory().code(),
                "Должна остаться транзакция с CAT2");

        System.out.println("✓ Удаление работает: было " + beforeDelete.size() +
                ", стало " + afterDelete.size());
    }

    @Test
    @DisplayName("Автоматическое создание папки data")
    void testDirectoryCreation() throws Exception {
        if (Files.exists(projectDataDir)) {
            Files.walk(projectDataDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); }
                        catch (IOException ignored) {}
                    });
            System.out.println("Папка data удалена для теста");
        }

        assertFalse(Files.exists(projectDataDir),
                "Папка data не должна существовать перед тестом");

        Currency rub = Currency.getInstance("RUB");
        Category test = new Category("TEST", "Test Category");
        Transaction t = new Expense(
                LocalDate.now(),
                new Money(new BigDecimal("100"), rub),
                test,
                "Тест создания директории"
        );

        fileStore.saveTransactions(Collections.singletonList(t));

        assertTrue(Files.exists(projectDataDir),
                "FileStore должен создать папку data");
        assertTrue(Files.isDirectory(projectDataDir),
                "data должна быть директорией");

        Path ledgerFile = projectDataDir.resolve("transactions.dat");
        assertTrue(Files.exists(ledgerFile),
                "Файл ledger.dat должен быть создан внутри data");

        System.out.println("✓ Папка data создана автоматически: " + projectDataDir);
    }

    @Test
    @DisplayName("Сохранение пустого списка")
    void testSaveEmptyList() throws Exception {
        List<Transaction> emptyList = new ArrayList<>();
        fileStore.saveTransactions(emptyList);

        List<Transaction> loaded = fileStore.loadTransactions();

        assertNotNull(loaded);
        assertTrue(loaded.isEmpty(), "Пустой список должен сохраняться и загружаться как пустой");

        System.out.println("✓ Пустой список корректно сохраняется и загружается");
    }

    @Test
    @DisplayName("Перезапись файла при повторном сохранении")
    void testOverwrite() throws Exception {
        Currency rub = Currency.getInstance("RUB");
        Category cat1 = new Category("OLD", "Old Category");

        List<Transaction> firstList = Arrays.asList(
                new Expense(LocalDate.now(),
                        new Money(new BigDecimal("1000"), rub),
                        cat1, "Старые данные")
        );
        fileStore.saveTransactions(firstList);

        Category cat2 = new Category("NEW", "New Category");
        List<Transaction> secondList = Arrays.asList(
                new Expense(LocalDate.now(),
                        new Money(new BigDecimal("2000"), rub),
                        cat2, "Новые данные")
        );
        fileStore.saveTransactions(secondList);

        List<Transaction> loaded = fileStore.loadTransactions();
        assertEquals(1, loaded.size());
        assertEquals("NEW", loaded.get(0).getCategory().code());
        assertEquals("Новые данные", loaded.get(0).getNote());

        System.out.println("✓ Файл корректно перезаписывается");
    }

    @AfterEach
    void cleanup() {
        try {
            Path ledgerFile = projectDataDir.resolve("transactions.dat");
            if (Files.exists(ledgerFile)) {
                long size = Files.size(ledgerFile);
                System.out.println("Файл после теста: " + ledgerFile +
                        " (" + size + " байт)");

                Files.deleteIfExists(ledgerFile);
                System.out.println("Тестовый файл удален");
            }
        } catch (IOException e) {
        }

        System.out.println("=== Тест завершен ===\n");
    }
}