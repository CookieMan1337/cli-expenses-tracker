package com.ledgerlite.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ledgerlite.domain.Budget;
import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Transaction;
import com.ledgerlite.service.ServiceContainer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FileStorage {
    private final ObjectMapper mapper;
    private final Path dataFile = Paths.get("./src/ledger.json");

    public FileStorage() {
        this.mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    public void save(ServiceContainer serviceContainer) {
        Map<String, Object> data = new HashMap<>();
        data.put("categories", serviceContainer.getAllCategories());
        data.put("transactions", serviceContainer.getAllTransactions());
        data.put("budgets", serviceContainer.getAllBudgets());

        try {
            mapper.writeValue(dataFile.toFile(), data);
        } catch (IOException exception) {
            log.error("Возникла ошибка при сохранении состояния в файл: {}", exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

    public void load(ServiceContainer serviceContainer) {
        if (!Files.exists(dataFile)) {
            log.info("Файл с данными не найден, запуск с пустым состоянием");
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(dataFile)) {
            if (!Files.exists(dataFile)) {
                log.info("Файл с данными не найден, запуск с пустым состоянием");
                return;
            }

            try {
                if (Files.size(dataFile) == 0) {
                    log.info("Файл с данными пустой, запуск с пустым состоянием");
                    return;
                }
            } catch (IOException exception) {
                log.error("Ошибка при проверке размера файла: {}", exception.getMessage());
                throw new RuntimeException("Не удалось проверить файл", exception);
            }

            Map<String, Object> data = mapper.readValue(br, new TypeReference<>() {});
            if (data.containsKey("categories")) {
                List<Category> categories = mapper.convertValue(
                        data.get("categories"),
                        new TypeReference<>() {
                        }
                );
                serviceContainer.loadAllCategories(categories);
                log.info("Категории загружены из файла {}", dataFile);
            }

            if (data.containsKey("transactions")) {
                List<Transaction> transactions = mapper.convertValue(data.get("transactions"),
                        new TypeReference<>() {
                        });
                serviceContainer.loadTransactions(transactions);
                log.info("Транзакции загружены из файла {}", dataFile);
            }

            if (data.containsKey("budgets")) {
                List<Budget> budgets = mapper.convertValue(data.get("budgets"),
                        new TypeReference<>() {
                        });
                serviceContainer.loadBudgets(budgets);
                log.info("Бюджеты загружены из файла {}", dataFile);
            }

            log.info("Данные загружены из файла: {}", dataFile);
        } catch (Exception e) {
            log.error("Ошибка при загрузке данных: {}", e.getMessage());
            throw new RuntimeException("Не удалось загрузить данные", e);
        }
    }
}