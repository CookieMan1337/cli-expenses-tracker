package com.ledgerlite.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ledgerlite.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileStorage {
    private static final Logger log = LoggerFactory.getLogger(FileStorage.class);

    private static final String DATA_DIR = "data";
    private final Path dataDir;
    private final ObjectMapper mapper;

    public FileStorage() {
        this.dataDir = Paths.get(DATA_DIR);
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        createDataDirectory();
    }

    // ===== ТРАНЗАКЦИИ =====

    public void saveTransactions(Collection<Transaction> transactions) throws IOException {
        Path file = dataDir.resolve("transactions.json");
        mapper.writeValue(file.toFile(), transactions);
        log.info("✅ Сохранено {} транзакций", transactions.size());
    }

    public List<Transaction> loadTransactions() throws IOException {
        Path file = dataDir.resolve("transactions.json");
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        List<Transaction> transactions = mapper.readValue(
                file.toFile(),
                new TypeReference<List<Transaction>>() {}
        );
        log.info("✅ Загружено {} транзакций", transactions.size());
        return transactions;
    }

    // ===== КАТЕГОРИИ =====

    public void saveCategories(Collection<Category> categories) throws IOException {
        Path file = dataDir.resolve("categories.json");
        mapper.writeValue(file.toFile(), categories);
        log.info("✅ Сохранено {} категорий", categories.size());
    }

    public List<Category> loadCategories() throws IOException {
        Path file = dataDir.resolve("categories.json");
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        List<Category> categories = mapper.readValue(
                file.toFile(),
                new TypeReference<List<Category>>() {}
        );
        log.info("✅ Загружено {} категорий", categories.size());
        return categories;
    }

    // ===== БЮДЖЕТЫ =====

    public void saveBudgets(Collection<Budget> budgets) throws IOException {
        Path file = dataDir.resolve("budgets.json");
        mapper.writeValue(file.toFile(), budgets);
        log.info("✅ Сохранено {} бюджетов", budgets.size());
    }

    public List<Budget> loadBudgets() throws IOException {
        Path file = dataDir.resolve("budgets.json");
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        List<Budget> budgets = mapper.readValue(
                file.toFile(),
                new TypeReference<List<Budget>>() {}
        );
        log.info("✅ Загружено {} бюджетов", budgets.size());
        return budgets;
    }

    // ===== ОБЩИЕ МЕТОДЫ =====

    private void createDataDirectory() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать директорию {}: {}", dataDir, e.getMessage());
        }
    }

    //Сохранить все данные приложения
    public void saveAll(Collection<Transaction> transactions,
                        Collection<Category> categories,
                        Collection<Budget> budgets) throws IOException {
        saveTransactions(transactions);
        saveCategories(categories);
        saveBudgets(budgets);
        log.info("✅ Все данные сохранены");
    }


    public boolean hasSavedData() {
        return Files.exists(dataDir.resolve("transactions.json")) ||
                Files.exists(dataDir.resolve("categories.json")) ||
                Files.exists(dataDir.resolve("budgets.json"));
    }


    public void clear() throws IOException {
        Files.deleteIfExists(dataDir.resolve("transactions.json"));
        Files.deleteIfExists(dataDir.resolve("categories.json"));
        Files.deleteIfExists(dataDir.resolve("budgets.json"));
        log.info("🗑️ Все данные удалены");
    }
}
